# dual_model_service.py
from fastapi import FastAPI
from pydantic import BaseModel
from transformers import pipeline
import torch
import logging
import os
from typing import Union

# 配置日志
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = FastAPI(
    title="双模型推理服务",
    description="同时提供情感分析和主题分类服务",
    version="1.0.0"
)

class TextRequest(BaseModel):
    text: str

class PredictionResponse(BaseModel):
    label: Union[str, int]
    score: float
    model: str

# 模型初始化函数
def load_model(model_path, task, model_name):
    logger.info(f"正在加载模型: {model_name} ({model_path})")
    try:
        # 自动检测 GPU 并启用半精度
        device = 0 if torch.cuda.is_available() else -1
        dtype = torch.float16 if device == 0 else torch.float32
        
        pipe = pipeline(
            task=task,
            model=model_path,
            tokenizer=model_path,
            device=device,
            torch_dtype=dtype
        )
        
        logger.info(f"{model_name} 加载成功! 设备: {'GPU' if device == 0 else 'CPU'}")
        return pipe
    except Exception as e:
        logger.error(f"加载模型 {model_name} 失败: {str(e)}")
        raise RuntimeError(f"无法加载模型 {model_name}")

# 加载两个模型
MOOD_SCORE_PATH = "./model/bert_4_moods"
DEPRESS_CHECK_PATH = "./model/bert-depress-check"

mood_score_pipeline = load_model(MOOD_SCORE_PATH, "text-classification", "情绪评分模型")
depress_check_pipeline = load_model(DEPRESS_CHECK_PATH, "text-classification", "抑郁检查模型")

@app.post("/mood_score", response_model=PredictionResponse, summary="情绪评分")
async def mood_score(request: TextRequest):
    """
    对输入文本进行情绪评分，返回情绪标签和置信度
    
    - **text**: 需要分析的文本
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
    
    - **text**: 需要分析的文本
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
    import uvicorn
    # 生产环境使用 8000 端口
    uvicorn.run(app, host="0.0.0.0", port=8000)