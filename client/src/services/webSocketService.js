import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';
import { AUTH_CONFIG } from "../utils/constants";

/**
 * 基于SockJS和STOMP.js的WebSocket服务类
 * 支持JWT认证、自动重连、消息订阅和发送
 */
class WebSocketService {
    /**
     * 构造函数
     * @param {Object} options 配置选项
     * @param {string} options.serverUrl WebSocket服务器地址
     * @param {number} [options.reconnectInterval=5000] 重连间隔(毫秒)
     * @param {number} [options.maxReconnectAttempts=10] 最大重连次数
     * @param {boolean} [options.debug=false] 是否启用调试模式
     */
    constructor(options = {}) {
        // 默认配置
        this.defaultOptions = {
            serverUrl: '/ws',
            reconnectInterval: 5000,
            maxReconnectAttempts: 10,
            debug: false
        };

        // 合并配置
        this.options = { ...this.defaultOptions, ...options };

        // STOMP客户端
        this.stompClient = null;

        // 连接状态
        this.connected = false;

        // 重连相关
        this.reconnectAttempts = 0;

        // 订阅主题集合
        this.subscriptions = {};

        // 事件监听器
        this.listeners = {
            connect: [],
            disconnect: [],
            error: [],
            message: []
        };
    }

    /**
     * 连接WebSocket服务器
     * @param {string} [jwt] JWT令牌（用于认证）
     */
    connect(jwt) {
        if (this.connected) {
            this._log('WebSocket already connected');
            return;
        }

        this.jwt = jwt;

        // 创建SockJS连接
        const socket = new SockJS(`${this.options.serverUrl}?token=${this.jwt}`);

        // 创建STOMP客户端
        this.stompClient = new Client({
            webSocketFactory: () => socket,
            debug: this.options.debug ? console.log : () => {},
            reconnectDelay: this.options.reconnectInterval,
            heartbeatIncoming: 4000,
            heartbeatOutgoing: 4000
        });

        // 设置连接回调
        this.stompClient.onConnect = (frame) => {
            this._handleConnect(frame);
        };

        // 设置错误回调
        this.stompClient.onStompError = (frame) => {
            this._handleError(frame);
        };

        // 设置断开连接回调
        this.stompClient.onDisconnect = () => {
            this._handleDisconnect();
        };

        // 准备连接头
        const headers = {};
        if (this.jwt) {
            headers.Authorization = `Bearer ${this.jwt}`;
        }

        // 激活连接
        this.stompClient.activate();

        this._log('Connecting to WebSocket server...');
    }

    /**
     * 断开WebSocket连接
     */
    disconnect() {
        if (this.stompClient && this.connected) {
            this.stompClient.deactivate();
            this.connected = false;
            this.subscriptions = {};
            this._log('WebSocket disconnected');
        }
    }

    /**
     * 订阅主题
     * @param {string} destination 目标主题（如 '/topic/public' 或 '/user/queue/private'）
     * @param {Function} callback 消息回调函数
     * @returns {string} 订阅ID
     */
    subscribe(destination, callback) {
        if (!destination) {
            this._log('Subscription failed: destination is required', 'error');
            return null;
        }

        // 存储订阅
        this.subscriptions[destination] = callback;

        // 如果已连接，立即订阅
        if (this.connected) {
            this._subscribe(destination);
        }

        this._log(`Subscribed to: ${destination}`);

        return destination;
    }

    /**
     * 取消订阅
     * @param {string} destination 目标主题
     * @returns {boolean} 是否取消成功
     */
    unsubscribe(destination) {
        if (this.subscriptions[destination]) {
            delete this.subscriptions[destination];
            this._log(`Unsubscribed from: ${destination}`);
            return true;
        }
        return false;
    }

    /**
     * 发送消息
     * @param {string} destination 目标地址
     * @param {Object|string} body 消息体
     * @param {Object} [headers={}] 额外头信息
     */
    send(destination, body, headers = {}) {
        if (!this.connected) {
            this._log('Cannot send message: not connected', 'error');
            return;
        }

        // 合并头信息（包括JWT）
        const mergedHeaders = { ...headers };
        if (this.jwt) {
            mergedHeaders.Authorization = `Bearer ${this.jwt}`;
        }

        // 发送消息
        this.stompClient.publish({
            destination,
            headers: mergedHeaders,
            body: JSON.stringify(body)
        });

        this._log(`Message sent to ${destination}`);
    }

    /**
     * 添加事件监听器
     * @param {string} event 事件类型 ('connect', 'disconnect', 'error', 'message')
     * @param {Function} callback 回调函数
     */
    on(event, callback) {
        if (this.listeners[event]) {
            this.listeners[event].push(callback);
        }
    }

    /**
     * 移除事件监听器
     * @param {string} event 事件类型
     * @param {Function} callback 要移除的回调函数
     */
    off(event, callback) {
        if (this.listeners[event]) {
            this.listeners[event] = this.listeners[event].filter(cb => cb !== callback);
        }
    }

    /**********************
     * 内部方法
     **********************/

    // 处理连接成功
    _handleConnect(frame) {
        this.connected = true;
        this.reconnectAttempts = 0;
        this._log('WebSocket connected successfully');

        // 通知连接监听器
        this._notifyListeners('connect', frame);

        // 重新订阅之前的主题
        Object.keys(this.subscriptions).forEach(destination => {
            this._subscribe(destination);
        });
    }

    // 处理错误
    _handleError(frame) {
        this._log(`WebSocket error: ${frame.headers?.message || 'Unknown error'}`, 'error');
        this._notifyListeners('error', frame);
        // 检查是否token失效
        if (frame.headers && frame.headers.message && (frame.headers.message.includes('认证') || frame.headers.message.toLowerCase().includes('authentication'))) {
            this._refreshTokenAndReconnect();
            return;
        }
        // 尝试重新连接
        if (!this.connected && this.reconnectAttempts < this.options.maxReconnectAttempts) {
            this.reconnectAttempts++;
            this._log(`Attempting reconnect (${this.reconnectAttempts}/${this.options.maxReconnectAttempts})...`);
            setTimeout(() => this.connect(this.jwt), this.options.reconnectInterval);
        }
    }

    // 处理断开连接
    _handleDisconnect() {
        this.connected = false;
        this._log('WebSocket disconnected');

        // 通知断开连接监听器
        this._notifyListeners('disconnect');

        // 尝试重新连接
        if (this.reconnectAttempts < this.options.maxReconnectAttempts) {
            this.reconnectAttempts++;
            this._log(`Attempting reconnect (${this.reconnectAttempts}/${this.options.maxReconnectAttempts})...`);
            setTimeout(() => this.connect(this.jwt), this.options.reconnectInterval);
        }
    }

    // 执行订阅
    _subscribe(destination) {
        const callback = this.subscriptions[destination];

        this.stompClient.subscribe(destination, (message) => {
            let parsedMessage;

            try {
                parsedMessage = JSON.parse(message.body);
            } catch (e) {
                parsedMessage = message.body;
            }

            // 调用订阅回调
            callback(parsedMessage);

            // 通知消息监听器
            this._notifyListeners('message', {
                destination,
                message: parsedMessage
            });
        });
    }

    // 通知所有监听器
    _notifyListeners(event, data) {
        if (this.listeners[event]) {
            this.listeners[event].forEach(callback => callback(data));
        }
    }

    // 日志记录
    _log(message, level = 'info') {
        if (!this.options.debug && level === 'info') return;

        const prefix = '[WebSocketService]';
        const timestamp = new Date().toISOString();

        switch (level) {
            case 'error':
                console.error(`${prefix} [${timestamp}] ${message}`);
                break;
            case 'warn':
                console.warn(`${prefix} [${timestamp}] ${message}`);
                break;
            default:
                console.log(`${prefix} [${timestamp}] ${message}`);
        }
    }

    async _refreshTokenAndReconnect() {
        // 获取当前用户id
        const userInfo = localStorage.getItem(AUTH_CONFIG.USER_INFO_KEY);
        let userId = null;
        if (userInfo) {
            try {
                userId = JSON.parse(userInfo).id;
            } catch {}
        }
        if (!userId) {
            alert('登录已失效，请重新登录');
            window.location.href = '/login';
            return;
        }
        const refreshToken = localStorage.getItem(`${AUTH_CONFIG.REFRESH_TOKEN_KEY}_${userId}`);
        if (!refreshToken) {
            alert('登录已失效，请重新登录');
            window.location.href = '/login';
            return;
        }
        try {
            const res = await fetch('http://localhost:8080/auth/refresh', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ refreshToken }),
            });
            if (res.ok) {
                const data = await res.json();
                localStorage.setItem(`${AUTH_CONFIG.TOKEN_KEY}_${userId}`, data.accessToken);
                localStorage.setItem(`${AUTH_CONFIG.REFRESH_TOKEN_KEY}_${userId}`, data.refreshToken);
                // 用新token重连
                this.connect(data.accessToken);
            } else {
                alert('登录已失效，请重新登录');
                window.location.href = '/login';
            }
        } catch {
            alert('登录已失效，请重新登录');
            window.location.href = '/login';
        }
    }
}

export default WebSocketService;