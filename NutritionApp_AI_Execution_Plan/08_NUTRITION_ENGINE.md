# Nutrition Engine

## Responsabilidades

- convertir porciones a gramos/ml;
- escalar nutrientes;
- sumar meals/day;
- calcular BMR/TDEE/targets;
- manejar precisión;
- producir snapshots históricos.

## Ejemplo

Si arroz cocido tiene 130 kcal/100g:

`180g -> 130 * 180 / 100 = 234 kcal`

## Objetivos

Inputs:
- sexo;
- edad;
- altura;
- peso;
- actividad;
- objetivo;
- ritmo.

Outputs:
- BMR;
- TDEE;
- calorie target;
- protein;
- carbs;
- fat.

La fórmula exacta debe convertirse en ADR y versionarse.

## Integridad histórica

Al crear `meal_entry`, copiar valores nutricionales calculados al snapshot.

No recalcular automáticamente entradas antiguas cuando cambie el catálogo.

## Precision
Calcular con alta precisión; redondear para presentación.

## Tests obligatorios
- 100g;
- fracciones;
- porciones;
- mixed meal;
- daily aggregation;
- rounding;
- catálogo modificado no altera snapshot.
