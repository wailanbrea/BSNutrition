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
        Schema::create('nutrition_goals', function (Blueprint $table) {
            $table->id();
            $table->foreignId('user_id')->constrained('users')->cascadeOnDelete();
            $table->date('effective_from');
            $table->unsignedInteger('calorie_target');
            $table->decimal('protein_target_g', 8, 2);
            $table->decimal('carbohydrate_target_g', 8, 2);
            $table->decimal('fat_target_g', 8, 2);
            $table->decimal('fiber_target_g', 8, 2)->nullable();
            $table->unsignedInteger('water_target_ml')->nullable();
            $table->string('source', 32)->default('calculated'); // 'calculated' | 'custom'
            $table->string('calculation_version', 32)->default('mifflin_v1.0');
            $table->timestamps();

            $table->index(['user_id', 'effective_from']);
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::dropIfExists('nutrition_goals');
    }
};
