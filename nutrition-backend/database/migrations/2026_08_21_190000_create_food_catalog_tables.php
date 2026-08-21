<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    /**
     * Run the migrations.
     */
    public function up(): void
    {
        // 1. Food Categories
        Schema::create('food_categories', function (Blueprint $table) {
            $table->id();
            $table->string('name', 128);
            $table->string('slug', 128)->unique();
            $table->string('icon', 64)->nullable();
            $table->timestamps();
        });

        // 2. Food Brands
        Schema::create('food_brands', function (Blueprint $table) {
            $table->id();
            $table->string('name', 128);
            $table->string('normalized_name', 128)->index();
            $table->string('country_code', 8)->nullable();
            $table->timestamps();
        });

        // 3. Food Sources (USDA, OpenFoodFacts, Custom, etc.)
        Schema::create('food_sources', function (Blueprint $table) {
            $table->id();
            $table->string('code', 64)->unique(); // 'usda_fdc', 'openfoodfacts', 'custom', 'generic'
            $table->string('name', 128);
            $table->string('url')->nullable();
            $table->string('version', 32)->nullable();
            $table->timestamps();
        });

        // 4. Canonical Nutrients
        Schema::create('nutrients', function (Blueprint $table) {
            $table->id();
            $table->string('code', 64)->unique(); // 'calories', 'protein', 'carbohydrate', 'total_fat', etc.
            $table->string('name', 128);
            $table->string('unit', 16); // 'kcal', 'g', 'mg', 'mcg', 'iu'
            $table->text('description')->nullable();
            $table->boolean('is_macro')->default(false);
            $table->unsignedSmallInteger('sort_order')->default(0);
            $table->timestamps();
        });

        // 5. Foods
        Schema::create('foods', function (Blueprint $table) {
            $table->id();
            $table->string('canonical_name', 255);
            $table->string('normalized_name', 255)->index();
            $table->foreignId('brand_id')->nullable()->constrained('food_brands')->nullOnDelete();
            $table->foreignId('category_id')->nullable()->constrained('food_categories')->nullOnDelete();
            $table->foreignId('user_id')->nullable()->constrained('users')->cascadeOnDelete(); // for custom foods
            $table->string('country_code', 8)->nullable();
            $table->string('language', 8)->default('es');
            $table->boolean('verified')->default(false);
            $table->string('source', 64)->default('generic'); // 'usda', 'openfoodfacts', 'user', 'generic'
            $table->string('external_source_id', 128)->nullable()->index();
            $table->decimal('default_basis_amount', 8, 2)->default(100.00);
            $table->string('default_basis_unit', 16)->default('g');
            $table->timestamps();
            $table->softDeletes();

            $table->index(['verified', 'source']);
            $table->index(['language', 'normalized_name']);
        });

        // 6. Food Aliases (for alternate search terms)
        Schema::create('food_aliases', function (Blueprint $table) {
            $table->id();
            $table->foreignId('food_id')->constrained('foods')->cascadeOnDelete();
            $table->string('alias', 255);
            $table->string('normalized_alias', 255)->index();
            $table->string('language', 8)->default('es');
            $table->timestamps();

            $table->index(['food_id', 'language']);
        });

        // 7. Food Barcodes
        Schema::create('food_barcodes', function (Blueprint $table) {
            $table->id();
            $table->foreignId('food_id')->constrained('foods')->cascadeOnDelete();
            $table->string('barcode', 64)->unique();
            $table->string('barcode_type', 32)->default('EAN_13');
            $table->boolean('is_primary')->default(true);
            $table->timestamps();
        });

        // 8. Food Portions
        Schema::create('food_portions', function (Blueprint $table) {
            $table->id();
            $table->foreignId('food_id')->constrained('foods')->cascadeOnDelete();
            $table->string('portion_name', 128); // '1 taza', '1 rebanada', '1 porción'
            $table->decimal('gram_weight', 8, 2); // weight in grams of this portion
            $table->decimal('amount', 8, 2)->default(1.00);
            $table->string('unit', 32)->default('porción');
            $table->boolean('is_default')->default(false);
            $table->timestamps();

            $table->index(['food_id', 'is_default']);
        });

        // 9. Food Nutrients (Pivot / Normalized amounts per basis amount)
        Schema::create('food_nutrients', function (Blueprint $table) {
            $table->id();
            $table->foreignId('food_id')->constrained('foods')->cascadeOnDelete();
            $table->foreignId('nutrient_id')->constrained('nutrients')->cascadeOnDelete();
            $table->decimal('amount', 10, 4); // amount per basis amount (e.g. 24.5g protein per 100g)
            $table->decimal('basis_amount', 8, 2)->default(100.00);
            $table->string('basis_unit', 16)->default('g');
            $table->string('source', 64)->nullable();
            $table->timestamps();

            $table->unique(['food_id', 'nutrient_id']);
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::dropIfExists('food_nutrients');
        Schema::dropIfExists('food_portions');
        Schema::dropIfExists('food_barcodes');
        Schema::dropIfExists('food_aliases');
        Schema::dropIfExists('foods');
        Schema::dropIfExists('nutrients');
        Schema::dropIfExists('food_sources');
        Schema::dropIfExists('food_brands');
        Schema::dropIfExists('food_categories');
    }
};
