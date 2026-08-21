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
        Schema::create('diaries', function (Blueprint $table) {
            $table->id();
            $table->foreignId('user_id')->constrained('users')->cascadeOnDelete();
            $table->date('diary_date');
            $table->string('timezone', 50)->default('America/Santo_Domingo');
            $table->text('notes')->nullable();
            $table->timestamps();

            $table->unique(['user_id', 'diary_date'], 'user_diary_date_unique');
            $table->index(['user_id', 'diary_date']);
        });

        Schema::create('meals', function (Blueprint $table) {
            $table->id();
            $table->foreignId('diary_id')->constrained('diaries')->cascadeOnDelete();
            $table->string('meal_type', 30); // breakfast, lunch, dinner, snack_1, etc.
            $table->string('name', 100)->nullable();
            $table->unsignedInteger('sort_order')->default(0);
            $table->timestamps();

            $table->unique(['diary_id', 'meal_type'], 'diary_meal_type_unique');
            $table->index(['diary_id', 'sort_order']);
        });

        Schema::create('meal_entries', function (Blueprint $table) {
            $table->id();
            $table->string('client_id', 64)->nullable()->unique();
            $table->foreignId('meal_id')->constrained('meals')->cascadeOnDelete();
            $table->foreignId('food_id')->nullable()->constrained('foods')->nullOnDelete();
            $table->foreignId('portion_id')->nullable()->constrained('food_portions')->nullOnDelete();
            $table->string('custom_name', 150);
            $table->decimal('quantity', 8, 2)->default(1.00);
            $table->string('unit', 50)->default('porción');
            $table->decimal('grams', 8, 2)->default(100.00);
            $table->integer('calories_snapshot')->default(0);
            $table->decimal('protein_snapshot', 8, 2)->default(0.00);
            $table->decimal('carbs_snapshot', 8, 2)->default(0.00);
            $table->decimal('fat_snapshot', 8, 2)->default(0.00);
            $table->decimal('fiber_snapshot', 8, 2)->nullable();
            $table->decimal('sodium_snapshot', 8, 2)->nullable();
            $table->decimal('sugar_snapshot', 8, 2)->nullable();
            $table->json('nutrient_snapshot_json')->nullable();
            $table->string('source', 30)->default('catalog'); // catalog, barcode, ai_vision, quick_add, custom
            $table->unsignedInteger('version')->default(1);
            $table->timestamps();
            $table->softDeletes();

            $table->index(['meal_id', 'deleted_at']);
        });

        Schema::create('water_logs', function (Blueprint $table) {
            $table->id();
            $table->string('client_id', 64)->nullable()->unique();
            $table->foreignId('user_id')->constrained('users')->cascadeOnDelete();
            $table->date('log_date');
            $table->integer('amount_ml');
            $table->timestamp('occurred_at')->useCurrent();
            $table->string('source', 30)->default('manual');
            $table->unsignedInteger('version')->default(1);
            $table->timestamps();
            $table->softDeletes();

            $table->index(['user_id', 'log_date']);
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::dropIfExists('water_logs');
        Schema::dropIfExists('meal_entries');
        Schema::dropIfExists('meals');
        Schema::dropIfExists('diaries');
    }
};
