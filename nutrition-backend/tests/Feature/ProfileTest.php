<?php

use App\Models\User;
use App\Models\UserProfile;
use Illuminate\Foundation\Testing\RefreshDatabase;

uses(RefreshDatabase::class);

test('it rejects unauthenticated requests to get or update profile', function () {
    $this->getJson('/api/v1/profile')
        ->assertStatus(401)
        ->assertJson([
            'error' => [
                'code' => 'UNAUTHENTICATED',
            ],
        ]);

    $this->putJson('/api/v1/profile', ['height' => 175])
        ->assertStatus(401)
        ->assertJson([
            'error' => [
                'code' => 'UNAUTHENTICATED',
            ],
        ]);
});

test('it retrieves profile for authenticated user', function () {
    $user = User::factory()->create();
    UserProfile::create([
        'user_id' => $user->id,
        'birth_date' => '1995-06-15',
        'sex' => 'male',
        'height' => 178.5,
        'current_weight' => 82.0,
        'activity_level' => 'moderately_active',
        'goal_type' => 'lose_weight',
        'goal_weight' => 75.0,
        'weekly_goal_rate' => 0.5,
        'locale' => 'es',
        'country_code' => 'DO',
        'timezone' => 'America/Santo_Domingo',
        'unit_system' => 'metric',
    ]);

    $token = $user->createToken('test_token')->plainTextToken;

    $response = $this->withHeader('Authorization', "Bearer {$token}")
        ->getJson('/api/v1/profile');

    $response->assertStatus(200)
        ->assertJsonStructure([
            'profile' => [
                'user_id',
                'birth_date',
                'sex',
                'height',
                'current_weight',
                'activity_level',
                'goal_type',
                'goal_weight',
                'weekly_goal_rate',
                'locale',
                'country_code',
                'timezone',
                'unit_system',
                'created_at',
                'updated_at',
            ],
        ])
        ->assertJson([
            'profile' => [
                'user_id' => $user->id,
                'birth_date' => '1995-06-15',
                'sex' => 'male',
                'height' => 178.5,
                'current_weight' => 82.0,
                'activity_level' => 'moderately_active',
                'goal_type' => 'lose_weight',
                'goal_weight' => 75.0,
                'weekly_goal_rate' => 0.5,
                'country_code' => 'DO',
            ],
        ]);
});

test('it automatically creates default profile on GET if not existing yet', function () {
    $user = User::factory()->create();
    $token = $user->createToken('test_token')->plainTextToken;

    $response = $this->withHeader('Authorization', "Bearer {$token}")
        ->getJson('/api/v1/profile');

    $response->assertStatus(200)
        ->assertJson([
            'profile' => [
                'user_id' => $user->id,
            ],
        ]);

    $this->assertDatabaseHas('user_profiles', [
        'user_id' => $user->id,
    ]);
});

test('it updates profile for authenticated user via PUT /api/v1/profile', function () {
    $user = User::factory()->create();
    $token = $user->createToken('test_token')->plainTextToken;

    $payload = [
        'birth_date' => '1998-04-20',
        'sex' => 'female',
        'height' => 165.0,
        'current_weight' => 60.5,
        'activity_level' => 'lightly_active',
        'goal_type' => 'maintain_weight',
        'goal_weight' => 60.0,
        'weekly_goal_rate' => 0.0,
        'locale' => 'es',
        'country_code' => 'DO',
        'timezone' => 'America/Santo_Domingo',
        'unit_system' => 'metric',
    ];

    $response = $this->withHeader('Authorization', "Bearer {$token}")
        ->putJson('/api/v1/profile', $payload);

    $response->assertStatus(200)
        ->assertJson([
            'profile' => [
                'user_id' => $user->id,
                'sex' => 'female',
                'height' => 165.0,
                'current_weight' => 60.5,
                'activity_level' => 'lightly_active',
            ],
        ]);

    $this->assertDatabaseHas('user_profiles', [
        'user_id' => $user->id,
        'sex' => 'female',
        'height' => 165.0,
        'current_weight' => 60.5,
    ]);
});

test('it validates profile inputs with 422 VALIDATION_ERROR on invalid data', function () {
    $user = User::factory()->create();
    $token = $user->createToken('test_token')->plainTextToken;

    $response = $this->withHeader('Authorization', "Bearer {$token}")
        ->putJson('/api/v1/profile', [
            'sex' => 'alien',
            'height' => 5000,
            'current_weight' => -10,
            'activity_level' => 'superhuman',
            'timezone' => 'Invalid/Timezone',
        ]);

    $response->assertStatus(422)
        ->assertJson([
            'error' => [
                'code' => 'VALIDATION_ERROR',
            ],
        ])
        ->assertJsonStructure([
            'error' => [
                'code',
                'message',
                'fields' => [
                    'sex',
                    'height',
                    'current_weight',
                    'activity_level',
                    'timezone',
                ],
            ],
        ]);
});

test('it guarantees strict ownership isolation between different users', function () {
    $userA = User::factory()->create();
    $userB = User::factory()->create();

    UserProfile::create([
        'user_id' => $userA->id,
        'sex' => 'male',
        'height' => 180,
    ]);

    UserProfile::create([
        'user_id' => $userB->id,
        'sex' => 'female',
        'height' => 160,
    ]);

    $tokenA = $userA->createToken('token_a')->plainTextToken;

    $response = $this->withHeader('Authorization', "Bearer {$tokenA}")
        ->getJson('/api/v1/profile');

    $response->assertStatus(200)
        ->assertJson([
            'profile' => [
                'user_id' => $userA->id,
                'sex' => 'male',
                'height' => 180,
            ],
        ]);

    // Updating userA should not modify userB
    $this->withHeader('Authorization', "Bearer {$tokenA}")
        ->putJson('/api/v1/profile', [
            'height' => 185,
        ])->assertStatus(200);

    $this->assertDatabaseHas('user_profiles', [
        'user_id' => $userB->id,
        'height' => 160,
    ]);
});
