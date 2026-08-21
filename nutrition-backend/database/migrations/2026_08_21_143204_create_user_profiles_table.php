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
        Schema::create('user_profiles', function (Blueprint $table) {
            $table->id();
            $table->foreignId('user_id')->unique()->constrained('users')->cascadeOnDelete();
            $table->date('birth_date')->nullable();
            $table->string('sex', 20)->nullable();
            $table->decimal('height', 6, 2)->nullable();
            $table->decimal('current_weight', 6, 2)->nullable();
            $table->string('activity_level', 40)->nullable();
            $table->string('goal_type', 40)->nullable();
            $table->decimal('goal_weight', 6, 2)->nullable();
            $table->decimal('weekly_goal_rate', 4, 2)->nullable();
            $table->string('locale', 10)->default('es');
            $table->string('country_code', 2)->default('DO');
            $table->string('timezone', 50)->default('America/Santo_Domingo');
            $table->string('unit_system', 20)->default('metric');
            $table->timestamps();
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::dropIfExists('user_profiles');
    }
};
