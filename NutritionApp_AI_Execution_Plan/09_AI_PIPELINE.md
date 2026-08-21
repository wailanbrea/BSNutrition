# AI Pipeline

## Principio

La IA reconoce y estima. El motor nutricional calcula.

## Foto

```text
Camera/Gallery
 -> resize/compress
 -> private upload
 -> AI provider
 -> structured detections
 -> Food Matching
 -> Nutrition Engine
 -> review
 -> correction
 -> confirm
 -> diary
```

## Salida estructurada

```json
{
  "foods": [
    {
      "name": "arroz blanco",
      "estimated_grams": 180,
      "food_confidence": 0.94,
      "portion_confidence": 0.72,
      "preparation_hint": "cocido"
    }
  ]
}
```

## Matching
Ranking con:
- exact/normalized name;
- aliases;
- preparación;
- locale/country;
- popularidad;
- historial usuario;
- marca;
- confidence.

## Confianza
Separar:
- food confidence;
- portion confidence;
- match confidence.

## Review
Usuario puede:
- cambiar alimento;
- cambiar gramos;
- eliminar;
- agregar;
- confirmar.

## Feedback
Guardar:
- alimento incorrecto;
- porción incorrecta;
- omitido;
- duplicado.

## Provider abstraction
OpenAI primero, pero provider-specific code no debe contaminar dominio.

## Cost
Registrar provider/model/tokens/images/cost/user/feature.

## Privacidad
Private storage + delete + retention policy.

## Evaluación futura
Dataset con platos dominicanos, platos mixtos, aceites/salsas, diferentes luces/porciones.
