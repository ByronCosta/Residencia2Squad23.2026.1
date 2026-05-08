import httpx 
import os 
from dotenv import load_dotenv

load_dotenv()

JAVA_API_URL = os.getenv("JAVA_API_URL" , "http://localhost:8080/salas/importar")

async def enviar_para_java(dados: dict) ->dict:
	async with httpx.AsyncClient() as client:
		try:
			response = await client.post(JAVA_API_URL, json=dados, timeout=10.0)
			response.raise_for_status()
			
			return {"sucesso": True, "status": response.status_code}
		except httpx.HTTPError as e:
			return{"sucesso": False, "erro": str(e)}

