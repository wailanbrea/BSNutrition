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
        Schema::create('ai_photo_analyses', function (Blueprint $table) {
            $table->id();
            $table->foreignId('user_id')->constrained()->cascadeOnDelete();
            $table->foreignId('upload_id')->nullable()->constrained('ai_image_uploads')->nullOnDelete();
            $table->string('status', 32)->default('pending');
            $table->string('dish_name', 255)->nullable();
            $table->text('summary')->nullable();
            $table->decimal('confidence_score', 4, 2)->nullable();
            $table->string('provider', 64)->default('openai');
            $table->string('model', 64)->default('gpt-4o-mini');
            $table->unsignedInteger('prompt_tokens')->default(0);
            $table->unsignedInteger('completion_tokens')->default(0);
            $table->decimal('estimated_cost_usd', 8, 6)->default(0.0);
            $table->unsignedInteger('total_calories')->default(0);
            $table->decimal('total_protein_g', 8, 2)->default(0.0);
            $table->decimal('total_carbs_g', 8, 2)->default(0.0);
            $table->decimal('total_fat_g', 8, 2)->default(0.0);
            $table->json('context')->nullable();
            $table->timestamps();

            $table->index(['user_id', 'status']);
        });

        Schema::create('ai_photo_analysis_items', function (Blueprint $table) {
            $table->id();
            $table->foreignId('analysis_id')->constrained('ai_photo_analyses')->cascadeOnDelete();
            $table->foreignId('food_id')->nullable()->constrained('foods')->nullOnDelete();
            $table->string('name', 255);
            $table->string('matched_name', 255)->nullable();
            $table->decimal('estimated_weight_grams', 8, 2)->default(100.0);
            $table->string('portion_description', 255)->nullable();
            $table->string('preparation_method', 255)->nullable();
            $table->decimal('confidence', 4, 2)->default(0.85);
            $table->unsignedInteger('calories')->default(0);
            $table->decimal('protein_g', 8, 2)->default(0.0);
            $table->decimal('carbs_g', 8, 2)->default(0.0);
            $table->decimal('fat_g', 8, 2)->default(0.0);
            $table->json('candidates')->nullable();
            $table->timestamps();

            $table->index('analysis_id');
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::dropIfExists('ai_photo_analysis_items');
        Schema::dropIfExists('ai_photo_analyses');
    }
};
