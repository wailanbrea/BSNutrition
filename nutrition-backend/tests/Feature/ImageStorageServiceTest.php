<?php

use App\Models\AiImageUpload;
use App\Models\User;
use App\Services\ImageStorageService;
use Illuminate\Http\UploadedFile;
use Illuminate\Support\Facades\Storage;

uses(\Illuminate\Foundation\Testing\RefreshDatabase::class);

beforeEach(function () {
    Storage::fake('local');
    $this->storageService = new ImageStorageService;
    $this->user = User::factory()->create();
});


test('storePrivateUpload saves image on private disk with metadata and expires_at', function () {
    $file = UploadedFile::fake()->image('food_plate.jpg', 640, 480);

    $upload = $this->storageService->storePrivateUpload($file, $this->user, 12);

    expect($upload)->toBeInstanceOf(AiImageUpload::class)
        ->and($upload->user_id)->toBe($this->user->id)
        ->and($upload->mime_type)->toBe('image/jpeg')
        ->and($upload->status)->toBe('uploaded')
        ->and($upload->retention_hours)->toBe(12)
        ->and($upload->expires_at)->not->toBeNull();

    Storage::disk('local')->assertExists($upload->path);
});

test('storePrivateUpload rejects invalid mime types', function () {
    $file = UploadedFile::fake()->create('document.pdf', 100, 'application/pdf');

    $this->storageService->storePrivateUpload($file, $this->user);
})->throws(InvalidArgumentException::class);

test('getImageBase64 returns valid base64 representation of image', function () {
    $file = UploadedFile::fake()->image('sancocho.png', 400, 400);
    $upload = $this->storageService->storePrivateUpload($file, $this->user);

    $base64 = $this->storageService->getImageBase64($upload);

    expect($base64)->toBeString()
        ->and(strlen($base64))->toBeGreaterThan(0)
        ->and(base64_decode($base64, true))->not->toBeFalse();
});

test('deleteUpload removes physical file and marks upload deleted', function () {
    $file = UploadedFile::fake()->image('mangu.webp', 500, 500);
    $upload = $this->storageService->storePrivateUpload($file, $this->user);

    $path = $upload->path;
    Storage::disk('local')->assertExists($path);

    $this->storageService->deleteUpload($upload);

    Storage::disk('local')->assertMissing($path);
    expect(AiImageUpload::find($upload->id))->toBeNull(); // Soft deleted
    expect(AiImageUpload::withTrashed()->find($upload->id)->status)->toBe('deleted');
});

test('cleanupExpiredUploads purges expired uploads and files', function () {
    $file1 = UploadedFile::fake()->image('expired.jpg', 300, 300);
    $upload1 = $this->storageService->storePrivateUpload($file1, $this->user);
    $upload1->update(['expires_at' => now()->subHour()]);

    $file2 = UploadedFile::fake()->image('valid.jpg', 300, 300);
    $upload2 = $this->storageService->storePrivateUpload($file2, $this->user);

    $cleanedCount = $this->storageService->cleanupExpiredUploads();

    expect($cleanedCount)->toBe(1);
    Storage::disk('local')->assertMissing($upload1->path);
    Storage::disk('local')->assertExists($upload2->path);
});
