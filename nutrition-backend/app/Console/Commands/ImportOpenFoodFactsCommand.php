<?php

namespace App\Console\Commands;

use App\Services\OpenFoodFactsService;
use Illuminate\Console\Command;

class ImportOpenFoodFactsCommand extends Command
{
    /**
     * The name and signature of the console command.
     *
     * @var string
     */
    protected $signature = 'foods:import-off 
                            {--barcode= : Código de barras del producto}
                            {--query= : Término de búsqueda de productos}
                            {--limit=5 : Límite de productos a importar en búsqueda}';

    /**
     * The console command description.
     *
     * @var string
     */
    protected $description = 'Importa alimentos desde Open Food Facts por código de barras o búsqueda';

    /**
     * Execute the console command.
     */
    public function handle(OpenFoodFactsService $offService): int
    {
        $barcode = $this->option('barcode');
        $query = $this->option('query');
        $limit = (int) $this->option('limit');

        if ($barcode) {
            $this->info("Buscando código de barras: {$barcode}...");
            $food = $offService->getByBarcode($barcode);

            if ($food) {
                $this->info("✓ Alimento importado exitosamente: [{$food->id}] {$food->canonical_name} ({$food->nutrients->count()} nutrientes)");

                return Command::SUCCESS;
            }

            $this->error("✗ No se encontró producto con código de barras: {$barcode}");

            return Command::FAILURE;
        }

        if ($query) {
            $this->info("Buscando en Open Food Facts para '{$query}'...");
            $results = $offService->search($query, $limit);
            $products = $results['products'] ?? [];

            if (empty($products)) {
                $this->warn("No se encontraron resultados para '{$query}'.");

                return Command::SUCCESS;
            }

            $count = 0;
            foreach ($products as $prod) {
                $food = $offService->importProduct($prod);
                if ($food) {
                    $count++;
                    $this->info("✓ [{$food->id}] {$food->canonical_name}");
                }
            }

            $this->info("✓ Total importados: {$count}");

            return Command::SUCCESS;
        }

        $this->error('Debes proporcionar --barcode o --query.');

        return Command::INVALID;
    }
}
