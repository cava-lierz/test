from fastapi import FastAPI
import uvicorn
from pydantic import BaseModel
from optimum.onnxruntime import ORTModelForSequenceClassification
from transformers import AutoTokenizer, pipeline
import logging
import os
from typing import Union

HOST="127.0.0.1"
PORT=8082

# 配置日志      
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = FastAPI(
    title="BERT模型推理服务",
    description="同时提供情感分析和主题分类服务",
    version="1.0.0"
)

class TextRequest(BaseModel):
    text: str

class PredictionResponse(BaseModel):
    label: Union[str, int]
    score: float
    model: str

# 模型初始化函数 - 使用 ONNX Runtime 加速
def load_model(model_path, task, model_name):
    logger.info(f"正在加载模型: {model_name} ({model_path})")
    try:
        # 使用 Optimum + ONNX Runtime 进行 CPU 加速
        logger.info(f"使用 ONNX Runtime 优化 CPU 推理...")
        
        # 加载优化后的模型
        model = ORTModelForSequenceClassification.from_pretrained(
            model_path,
            export=True,  # 自动转换为 ONNX 格式
            provider="CPUExecutionProvider"  # 使用 CPU 执行提供程序
        )
        
        # 加载 tokenizer
        tokenizer = AutoTokenizer.from_pretrained(model_path)
        
        # 创建优化的 pipeline
        pipe = pipeline(
            task=task,
            model=model,
            tokenizer=tokenizer,
            device=-1  # CPU
        )
        
        logger.info(f"{model_name} 加载成功! 使用 ONNX Runtime CPU 加速")
        return pipe
    except Exception as e:
        logger.error(f"加载模型 {model_name} 失败: {str(e)}")
        raise RuntimeError(f"无法加载模型 {model_name}")

# 加载两个模型
# 模型路径 - 相对于项目根目录
MOOD_SCORE_PATH = os.getenv("MOOD_SCORE_PATH", "model/bert_4_moods")
DEPRESS_CHECK_PATH = os.getenv("DEPRESS_CHECK_PATH", "model/bert-depress-check")

mood_score_pipeline = load_model(MOOD_SCORE_PATH, "text-classification", "情绪评分模型")
depress_check_pipeline = load_model(DEPRESS_CHECK_PATH, "text-classification", "抑郁检查模型")

@app.post("/mood_score", response_model=PredictionResponse, summary="情绪评分")
async def mood_score(request: TextRequest):
    """
    对输入文本进行情绪评分，返回情绪标签和置信度
    text: 需要分析的文本
    """
    try:

        logger.info(f"开始情绪评分: {request.text}")
        result = mood_score_pipeline(request.text)
        return {
            "label": result[0]["label"],
            "score": float(result[0]["score"]),
            "model": "bert_4_moods"
        }
    except Exception as e:
        logger.error(f"情感分析出错: {str(e)}")
        return {"error": "处理请求时出错"}, 500

@app.post("/depress_check", response_model=PredictionResponse, summary="抑郁检查")
async def depress_check(request: TextRequest):
    """
    对输入文本进行主题分类，返回主题标签和置信度
    
    text: 需要分析的文本
    """
    try:
        result = depress_check_pipeline(request.text)
        return {
            "label": result[0]["label"],
            "score": float(result[0]["score"]),
            "model": "bert_depress_check"
        }
    except Exception as e:
        logger.error(f"主题分类出错: {str(e)}")
        return {"error": "处理请求时出错"}, 500

if __name__ == "__main__":
    uvicorn.run(app, host=HOST, port=PORT)