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
        Schema::create('user_subscriptions', function (Blueprint $table) {
            $table->id();
            $table->foreignId('user_id')->constrained('users')->cascadeOnDelete();
            $table->string('plan_id', 50)->default('pro_monthly');
            $table->string('status', 30)->default('active'); // active, in_grace_period, expired, canceled
            $table->string('provider', 30)->default('google_play'); // google_play, mock_store, manual
            $table->text('purchase_token')->nullable();
            $table->string('order_id', 100)->nullable();
            $table->timestamp('starts_at')->useCurrent();
            $table->timestamp('expires_at')->nullable();
            $table->boolean('auto_renewing')->default(true);
            $table->timestamps();

            $table->index(['user_id', 'status']);
        });

        Schema::create('user_daily_ai_quotas', function (Blueprint $table) {
            $table->id();
            $table->foreignId('user_id')->constrained('users')->cascadeOnDelete();
            $table->date('quota_date');
            $table->unsignedInteger('photo_analyses_count')->default(0);
            $table->unsignedInteger('text_parses_count')->default(0);
            $table->timestamps();

            $table->unique(['user_id', 'quota_date']);
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::dropIfExists('user_daily_ai_quotas');
        Schema::dropIfExists('user_subscriptions');
    }
};
