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
        Schema::create('weight_logs', function (Blueprint $table) {
            $table->id();
            $table->string('client_id', 64)->nullable()->unique();
            $table->foreignId('user_id')->constrained('users')->cascadeOnDelete();
            $table->date('log_date');
            $table->decimal('weight_kg', 5, 2);
            $table->timestamp('occurred_at')->useCurrent();
            $table->string('source', 30)->default('manual'); // manual, health_connect, smart_scale
            $table->string('notes', 255)->nullable();
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
        Schema::dropIfExists('weight_logs');
    }
};
