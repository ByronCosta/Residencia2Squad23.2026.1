from fastapi import FastAPI, UploadFile, File, HTTPException
from services.ia import analisar_planta
from services.java_client import enviar_para_java
import shutil
import os

app = FastAPI()

UPLOAD_DIR = "uploads"
os.makedirs(UPLOAD_DIR, exist_ok=True)

@app.post("/analisar")
async def analisar(file: UploadFile = File(...)):
    # 1. Validação de Extensão
    if not file.filename.lower().endswith((".png", ".jpg", ".jpeg")):
        raise HTTPException(status_code=400, detail="Apenas imagens PNG ou JPG")

    caminho = os.path.join(UPLOAD_DIR, file.filename)

    # 2. Salvamento do arquivo usando context manager
    try:
        with open(caminho, "wb") as buffer:
            shutil.copyfileobj(file.file, buffer)
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Erro ao salvar arquivo: {str(e)}")

    # 3. Processamento e Integração
    try:
        # Chama sua lógica de IA (YOLOv8)
        resultado = analisar_planta(caminho)
        
        # Envia para o Java (Certifique-se de ajustar a URL no java_client.py)
        resposta_java = await enviar_para_java(resultado)
        
        return {
            "analise": resultado, 
            "java": resposta_java
        }
    except Exception as e:
        # Log do erro para facilitar o debug no terminal
        print(f"Erro no processamento: {e}")
        raise HTTPException(status_code=500, detail=str(e))
    finally:
        # Opcional: Remover o arquivo após processar para não lotar o disco
        # if os.path.exists(caminho): os.remove(caminho)
        pass

@app.get("/")
def root():
    return {"status": "API rodando"}