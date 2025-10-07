#include "third_party/httplib.h"
#include "third_party/json.hpp"
#include "embedding/embedding.h"
#include <iostream>
#include <sstream>

using json = nlohmann::json;

class EmbeddingServer {
private:
    httplib::Server server;
    const int port;

public:
    EmbeddingServer(int port = 8080) : port(port) {
        setupRoutes();
    }

    void setupRoutes() {
        // 健康检查接口
        server.Get("/health", [](const httplib::Request&, httplib::Response& res) {
            res.set_content("{\"status\": \"ok\"}", "application/json");
        });

        // 单个文本embedding接口
        server.Post("/embedding", [](const httplib::Request& req, httplib::Response& res) {
            try {
                // 解析JSON请求
                json request_json = json::parse(req.body);
                
                if (!request_json.contains("text")) {
                    res.status = 400;
                    res.set_content("{\"error\": \"Missing 'text' field\"}", "application/json");
                    return;
                }

                std::string text = request_json["text"];
                if (text.empty()) {
                    res.status = 400;
                    res.set_content("{\"error\": \"Text cannot be empty\"}", "application/json");
                    return;
                }

                // 调用embedding函数
                std::vector<float> embedding = embedding_single(text);
                
                if (embedding.empty()) {
                    res.status = 500;
                    res.set_content("{\"error\": \"Failed to generate embedding\"}", "application/json");
                    return;
                }

                // 构建响应
                json response;
                response["embedding"] = embedding;
                response["dimension"] = embedding.size();
                
                res.set_content(response.dump(), "application/json");
                
            } catch (const json::exception& e) {
                res.status = 400;
                res.set_content("{\"error\": \"Invalid JSON format\"}", "application/json");
            } catch (const std::exception& e) {
                res.status = 500;
                res.set_content("{\"error\": \"Internal server error\"}", "application/json");
            }
        });

        // 批量文本embedding接口
        server.Post("/embedding/batch", [](const httplib::Request& req, httplib::Response& res) {
            try {
                // 解析JSON请求
                json request_json = json::parse(req.body);
                
                if (!request_json.contains("texts") || !request_json["texts"].is_array()) {
                    res.status = 400;
                    res.set_content("{\"error\": \"Missing 'texts' array field\"}", "application/json");
                    return;
                }

                std::vector<std::string> texts = request_json["texts"];
                if (texts.empty()) {
                    res.status = 400;
                    res.set_content("{\"error\": \"Texts array cannot be empty\"}", "application/json");
                    return;
                }

                // 将多个文本用换行符连接
                std::stringstream ss;
                for (size_t i = 0; i < texts.size(); ++i) {
                    if (i > 0) ss << "\n";
                    ss << texts[i];
                }
                std::string combined_text = ss.str();

                // 调用批量embedding函数
                std::vector<std::vector<float>> embeddings = embedding_batch(combined_text);
                
                if (embeddings.empty()) {
                    res.status = 500;
                    res.set_content("{\"error\": \"Failed to generate embeddings\"}", "application/json");
                    return;
                }

                // 构建响应
                json response;
                response["embeddings"] = embeddings;
                response["count"] = embeddings.size();
                if (!embeddings.empty()) {
                    response["dimension"] = embeddings[0].size();
                }
                
                res.set_content(response.dump(), "application/json");
                
            } catch (const json::exception& e) {
                res.status = 400;
                res.set_content("{\"error\": \"Invalid JSON format\"}", "application/json");
            } catch (const std::exception& e) {
                res.status = 500;
                res.set_content("{\"error\": \"Internal server error\"}", "application/json");
            }
        });

        // 设置CORS头
        server.set_default_headers({
            {"Access-Control-Allow-Origin", "*"},
            {"Access-Control-Allow-Methods", "GET, POST, OPTIONS"},
            {"Access-Control-Allow-Headers", "Content-Type"}
        });
    }

    void run() {
        std::cout << "Starting embedding server on port " << port << std::endl;
        std::cout << "Available endpoints:" << std::endl;
        std::cout << "  GET  /health" << std::endl;
        std::cout << "  POST /embedding" << std::endl;
        std::cout << "  POST /embedding/batch" << std::endl;
        
        if (!server.listen("0.0.0.0", port)) {
            std::cerr << "Failed to start server on port " << port << std::endl;
            exit(1);
        }
    }
};

int main(int argc, char* argv[]) {
    int port = 8081;
    std::string model_path = "./model/nomic-embed-text-v1.5.Q8_0.gguf";
    int n_gpu_layers = 9999;  // 使用所有GPU层
    
    // 解析命令行参数
    for (int i = 1; i < argc; i++) {
        std::string arg = argv[i];
        if (arg == "--port" && i + 1 < argc) {
            port = std::stoi(argv[++i]);
        } else if (arg == "--help" || arg == "-h") {
            std::cout << "Usage: " << argv[0] << " [--port PORT]" << std::endl;
            std::cout << "  --port PORT    Server port (default: 8080)" << std::endl;
            return 0;
        }
    }

    EmbeddingServer server(port);
    server.run();
    
    return 0;
} 