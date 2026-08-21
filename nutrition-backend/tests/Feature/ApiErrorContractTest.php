<?php

use Illuminate\Http\Request;
use Illuminate\Support\Facades\Route;

beforeEach(function () {
    Route::post('/api/v1/test-validation', function (Request $request) {
        $request->validate([
            'email' => 'required|email',
            'name' => 'required|string|min:3',
        ]);

        return response()->json(['status' => 'ok']);
    });

    Route::get('/api/v1/test-server-error', function () {
        throw new RuntimeException('Database server down');
    });
});

test('it returns structured 401 UNAUTHENTICATED error on protected routes without token', function () {
    $response = $this->getJson('/api/v1/user');

    $response->assertStatus(401)
        ->assertJson([
            'error' => [
                'code' => 'UNAUTHENTICATED',
                'message' => 'Unauthenticated.',
            ],
        ])
        ->assertJsonStructure([
            'error' => [
                'code',
                'message',
                'fields',
            ],
        ]);
});

test('it returns structured 404 NOT_FOUND error on non-existent api route', function () {
    $response = $this->getJson('/api/v1/unknown-endpoint');

    $response->assertStatus(404)
        ->assertJson([
            'error' => [
                'code' => 'NOT_FOUND',
                'message' => 'Resource not found.',
            ],
        ])
        ->assertJsonStructure([
            'error' => [
                'code',
                'message',
                'fields',
            ],
        ]);
});

test('it returns structured 405 METHOD_NOT_ALLOWED on wrong http verb', function () {
    $response = $this->postJson('/api/v1/health');

    $response->assertStatus(405)
        ->assertJson([
            'error' => [
                'code' => 'METHOD_NOT_ALLOWED',
            ],
        ]);
});

test('it returns structured 422 VALIDATION_ERROR with field details on invalid payload', function () {
    $response = $this->postJson('/api/v1/test-validation', [
        'email' => 'invalid-email',
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
                    'email',
                    'name',
                ],
            ],
        ]);
});

test('it guarantees json response without html even without accept json header', function () {
    $response = $this->get('/api/v1/unknown-route-without-headers');

    $response->assertStatus(404)
        ->assertHeader('Content-Type', 'application/json')
        ->assertJson([
            'error' => [
                'code' => 'NOT_FOUND',
            ],
        ]);
});

test('it returns structured 500 SERVER_ERROR on unhandled server exception', function () {
    $response = $this->getJson('/api/v1/test-server-error');

    $response->assertStatus(500)
        ->assertJsonStructure([
            'error' => [
                'code',
                'message',
                'fields',
            ],
        ])
        ->assertJson([
            'error' => [
                'code' => 'SERVER_ERROR',
            ],
        ]);
});
