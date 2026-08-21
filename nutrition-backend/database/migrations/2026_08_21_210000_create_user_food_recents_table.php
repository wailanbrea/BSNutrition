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
        Schema::create('user_food_recents', function (Blueprint $table) {
            $table->id();
            $table->foreignId('user_id')->constrained('users')->cascadeOnDelete();
            $table->foreignId('food_id')->constrained('foods')->cascadeOnDelete();
            $table->unsignedInteger('use_count')->default(1);
            $table->timestamp('last_used_at')->useCurrent();
            $table->timestamps();

            $table->unique(['user_id', 'food_id'], 'user_recent_food_unique');
            $table->index(['user_id', 'last_used_at']);
            $table->index(['user_id', 'use_count']);
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::dropIfExists('user_food_recents');
    }
};
