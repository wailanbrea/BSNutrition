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
        Schema::create('recipes', function (Blueprint $table) {
            $table->id();
            $table->foreignId('user_id')->nullable()->constrained('users')->cascadeOnDelete();
            $table->string('name', 200);
            $table->text('description')->nullable();
            $table->unsignedInteger('servings')->default(1);
            $table->unsignedInteger('prep_time_minutes')->nullable();
            $table->unsignedInteger('cook_time_minutes')->nullable();
            $table->decimal('total_weight_grams', 8, 2)->default(0.00);
            $table->integer('calories_per_serving')->default(0);
            $table->decimal('protein_per_serving_g', 8, 2)->default(0.00);
            $table->decimal('carbs_per_serving_g', 8, 2)->default(0.00);
            $table->decimal('fat_per_serving_g', 8, 2)->default(0.00);
            $table->decimal('fiber_per_serving_g', 8, 2)->nullable();
            $table->boolean('is_public')->default(false);
            $table->string('image_path')->nullable();
            $table->timestamps();
            $table->softDeletes();

            $table->index(['user_id', 'is_public']);
        });

        Schema::create('recipe_ingredients', function (Blueprint $table) {
            $table->id();
            $table->foreignId('recipe_id')->constrained('recipes')->cascadeOnDelete();
            $table->foreignId('food_id')->nullable()->constrained('foods')->nullOnDelete();
            $table->foreignId('portion_id')->nullable()->constrained('food_portions')->nullOnDelete();
            $table->string('custom_name', 150);
            $table->decimal('quantity', 8, 2)->default(1.00);
            $table->string('unit', 50)->default('g');
            $table->decimal('grams', 8, 2)->default(100.00);
            $table->integer('calories')->default(0);
            $table->decimal('protein_g', 8, 2)->default(0.00);
            $table->decimal('carbs_g', 8, 2)->default(0.00);
            $table->decimal('fat_g', 8, 2)->default(0.00);
            $table->unsignedInteger('sort_order')->default(0);
            $table->timestamps();

            $table->index(['recipe_id', 'sort_order']);
        });

        Schema::create('recipe_steps', function (Blueprint $table) {
            $table->id();
            $table->foreignId('recipe_id')->constrained('recipes')->cascadeOnDelete();
            $table->unsignedInteger('step_number')->default(1);
            $table->text('instruction');
            $table->timestamps();

            $table->index(['recipe_id', 'step_number']);
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::dropIfExists('recipe_steps');
        Schema::dropIfExists('recipe_ingredients');
        Schema::dropIfExists('recipes');
    }
};
