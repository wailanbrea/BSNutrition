<?php

test('it returns health status as json with ok status and db connectivity', function () {
    $response = $this->getJson('/api/v1/health');

    $response->assertStatus(200)
        ->assertJsonStructure([
            'status',
            'version',
            'environment',
            'timestamp',
            'services' => [
                'database',
            ],
        ])
        ->assertJson([
            'status' => 'ok',
            'services' => [
                'database' => 'ok',
            ],
        ]);
});
