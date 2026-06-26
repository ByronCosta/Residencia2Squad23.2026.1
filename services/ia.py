# ia.py
import json
from ultralytics import YOLO

model = YOLO("best.pt") 

def analisar_planta(caminho_imagem: str) -> dict:
    results = model(caminho_imagem)
    itens_detectados = []
    contador_estacao = 1
    
    for r in results:
        # r.names garante o dicionário de classes correto que você validou no console
        mapeamento_classes = r.names  
        
        for box in r.boxes:
            # .item() extrai o índice numérico exato do Tensor do YOLO
            class_id = int(box.cls[0].item())
            label_real = mapeamento_classes[class_id].lower() 
            
            # Padroniza o retorno para bater com as condições do Java
            if "design" in label_real:
                label = "design"
            elif "dev" in label_real:
                label = "dev"
            else:
                label = "simples"
            
            # Coordenadas do Box
            xyxy = box.xyxy[0].tolist()
            coord_x = int((xyxy[0] + xyxy[2]) / 2) # Centro X
            coord_y = int((xyxy[1] + xyxy[3]) / 2) # Centro Y
            
            estacao = {
                "estacao": contador_estacao,
                "tipo": label,  # Mudado de 'descricao' para 'tipo' para alinhar com o Java
                "coordx": coord_x,
                "coordy": coord_y
            }
            itens_detectados.append(estacao)
            contador_estacao += 1

    return {
        "itens": itens_detectados,
        "total_itens": len(itens_detectados),
        "observacoes_gerais": f"A IA detectou um total de {len(itens_detectados)} estações."
    }