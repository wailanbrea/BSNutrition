<?php

namespace App\Console\Commands;

use App\Services\UsdaFoodDataService;
use Illuminate\Console\Command;

class ImportUsdaFoodCommand extends Command
{
    /**
     * The name and signature of the console command.
     *
     * @var string
     */
    protected $signature = 'foods:import-usda 
                            {--query= : Término de búsqueda en USDA (ej: "apple", "chicken breast")}
                            {--fdcId= : FDC ID específico a importar}
                            {--limit=5 : Límite de alimentos a importar en búsqueda}';

    /**
     * The console command description.
     *
     * @var string
     */
    protected $description = 'Importa alimentos desde USDA FoodData Central a la base de datos local';

    /**
     * Execute the console command.
     */
    public function handle(UsdaFoodDataService $usdaService): int
    {
        $fdcId = $this->option('fdcId');
        $query = $this->option('query');
        $limit = (int) $this->option('limit');

        if ($fdcId) {
            $this->info("Importando alimento FDC ID: {$fdcId}...");
            $food = $usdaService->importFood($fdcId);

            if ($food) {
                $this->info("✓ Alimento importado exitosamente: [{$food->id}] {$food->canonical_name} ({$food->nutrients->count()} nutrientes, {$food->portions->count()} porciones)");

                return Command::SUCCESS;
            }

            $this->error("✗ No se pudo importar el alimento con FDC ID: {$fdcId}");

            return Command::FAILURE;
        }

        if ($query) {
            $this->info("Buscando en USDA para '{$query}' (límite: {$limit})...");
            $results = $usdaService->search($query, $limit);
            $foods = $results['foods'] ?? [];

            if (empty($foods)) {
                $this->warn("No se encontraron resultados en USDA para '{$query}'.");

                return Command::SUCCESS;
            }

            $this->info('Resultados encontrados: '.count($foods));
            $count = 0;

            foreach ($foods as $item) {
                $itemId = $item['fdcId'] ?? null;
                if (! $itemId) {
                    continue;
                }

                $this->line("-> Importando FDC {$itemId}: {$item['description']}...");
                $imported = $usdaService->importFood($itemId);
                if ($imported) {
                    $count++;
                    $this->info("   ✓ Guardado como ID {$imported->id}");
                }
            }

            $this->info("✓ Proceso completado: {$count} alimentos importados.");

            return Command::SUCCESS;
        }

        $this->error('Debes proporcionar la opción --query o --fdcId.');

        return Command::INVALID;
    }
}
