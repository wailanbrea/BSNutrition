<?php

use App\Models\User;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Illuminate\Support\Facades\Hash;

uses(RefreshDatabase::class);

test('it registers a new user and returns a bearer token and user resource', function () {
    $payload = [
        'name' => 'Wailan Brea',
        'email' => 'wailan@example.com',
        'password' => 'SecurePass123!',
        'password_confirmation' => 'SecurePass123!',
        'device_name' => 'Pixel 8 Pro',
    ];

    $response = $this->postJson('/api/v1/auth/register', $payload);

    $response->assertStatus(201)
        ->assertJsonStructure([
            'user' => [
                'id',
                'name',
                'email',
                'created_at',
                'updated_at',
            ],
            'token',
            'token_type',
        ])
        ->assertJson([
            'user' => [
                'name' => 'Wailan Brea',
                'email' => 'wailan@example.com',
            ],
            'token_type' => 'Bearer',
        ]);

    $this->assertDatabaseHas('users', [
        'email' => 'wailan@example.com',
    ]);
});

test('it fails registration with 422 validation error when email is already taken', function () {
    User::factory()->create([
        'email' => 'existing@example.com',
    ]);

    $response = $this->postJson('/api/v1/auth/register', [
        'name' => 'Another User',
        'email' => 'existing@example.com',
        'password' => 'Password123!',
        'password_confirmation' => 'Password123!',
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
                'fields' => ['email'],
            ],
        ]);
});

test('it fails registration with 422 when password confirmation does not match', function () {
    $response = $this->postJson('/api/v1/auth/register', [
        'name' => 'John Doe',
        'email' => 'john@example.com',
        'password' => 'Password123!',
        'password_confirmation' => 'DifferentPassword!',
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
                'fields' => ['password'],
            ],
        ]);
});

test('it logs in a user with valid credentials and returns a token', function () {
    $user = User::factory()->create([
        'email' => 'login@example.com',
        'password' => Hash::make('SecretPass123'),
    ]);

    $response = $this->postJson('/api/v1/auth/login', [
        'email' => 'login@example.com',
        'password' => 'SecretPass123',
        'device_name' => 'Samsung S24',
    ]);

    $response->assertStatus(200)
        ->assertJsonStructure([
            'user' => [
                'id',
                'name',
                'email',
            ],
            'token',
            'token_type',
        ])
        ->assertJson([
            'user' => [
                'id' => $user->id,
                'email' => 'login@example.com',
            ],
            'token_type' => 'Bearer',
        ]);
});

test('it fails login with 422 when credentials are invalid', function () {
    User::factory()->create([
        'email' => 'valid@example.com',
        'password' => Hash::make('CorrectPassword'),
    ]);

    $response = $this->postJson('/api/v1/auth/login', [
        'email' => 'valid@example.com',
        'password' => 'WrongPassword',
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
                'fields' => ['email'],
            ],
        ]);
});

test('it returns authenticated user profile via GET /api/v1/me', function () {
    $user = User::factory()->create([
        'name' => 'Auth User',
        'email' => 'auth@example.com',
    ]);

    $token = $user->createToken('test_token')->plainTextToken;

    $response = $this->withHeader('Authorization', "Bearer {$token}")
        ->getJson('/api/v1/me');

    $response->assertStatus(200)
        ->assertJson([
            'user' => [
                'id' => $user->id,
                'name' => 'Auth User',
                'email' => 'auth@example.com',
            ],
        ]);
});

test('it revokes the current token on POST /api/v1/auth/logout', function () {
    $user = User::factory()->create();
    $token = $user->createToken('logout_device')->plainTextToken;

    $this->assertCount(1, $user->tokens);

    $response = $this->withHeader('Authorization', "Bearer {$token}")
        ->postJson('/api/v1/auth/logout');

    $response->assertStatus(200)
        ->assertJson([
            'message' => 'Logged out successfully.',
        ]);

    $this->assertCount(0, $user->fresh()->tokens);
});

test('it deletes user account and revokes all tokens on DELETE /api/v1/me', function () {
    $user = User::factory()->create();
    $token = $user->createToken('active_token')->plainTextToken;

    $response = $this->withHeader('Authorization', "Bearer {$token}")
        ->deleteJson('/api/v1/me');

    $response->assertStatus(200)
        ->assertJson([
            'message' => 'Account deleted successfully.',
        ]);

    $this->assertDatabaseMissing('users', [
        'id' => $user->id,
    ]);
});
