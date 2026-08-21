<?php

namespace App\Services;

use App\Models\AiImageUpload;
use App\Models\User;
use Illuminate\Http\UploadedFile;
use Illuminate\Support\Facades\Storage;
use Illuminate\Support\Str;
use InvalidArgumentException;

class ImageStorageService
{
    private const ALLOWED_MIME_TYPES = [
        'image/jpeg',
        'image/jpg',
        'image/png',
        'image/webp',
        'image/heic',
    ];

    private const MAX_FILE_SIZE_BYTES = 10 * 1024 * 1024; // 10MB

    /**
     * Store an uploaded image securely on the private disk and register metadata.
     */
    public function storePrivateUpload(UploadedFile $file, User $user, int $retentionHours = 24): AiImageUpload
    {
        $mimeType = $file->getMimeType();
        if (! in_array($mimeType, self::ALLOWED_MIME_TYPES, true)) {
            throw new InvalidArgumentException("Tipo de imagen no permitido: {$mimeType}. Formatos válidos: JPEG, PNG, WEBP, HEIC.");
        }

        $fileSize = $file->getSize();
        if ($fileSize > self::MAX_FILE_SIZE_BYTES) {
            throw new InvalidArgumentException('El tamaño de la imagen supera el límite máximo de 10 MB.');
        }

        $extension = $file->getClientOriginalExtension() ?: 'jpg';
        $filename = (string) Str::uuid().'.'.$extension;
        $directory = "ai_uploads/{$user->id}";
        $path = "{$directory}/{$filename}";

        $disk = config('filesystems.default', 'local');
        $stored = Storage::disk($disk)->putFileAs($directory, $file, $filename);

        if (! $stored) {
            throw new \RuntimeException('No se pudo guardar la imagen en el almacenamiento privado.');
        }

        return AiImageUpload::create([
            'user_id' => $user->id,
            'disk' => $disk,
            'path' => $path,
            'original_name' => $file->getClientOriginalName(),
            'mime_type' => $mimeType,
            'file_size_bytes' => $fileSize,
            'status' => 'uploaded',
            'retention_hours' => $retentionHours,
            'expires_at' => now()->addHours($retentionHours),
        ]);
    }

    /**
     * Get the raw content bytes of the uploaded image.
     */
    public function getImageBytes(AiImageUpload $upload): string
    {
        if (! Storage::disk($upload->disk)->exists($upload->path)) {
            throw new \RuntimeException("El archivo de imagen no existe en la ruta: {$upload->path}");
        }

        return (string) Storage::disk($upload->disk)->get($upload->path);
    }

    /**
     * Get the base64-encoded string of the uploaded image for AI Vision analysis.
     */
    public function getImageBase64(AiImageUpload $upload): string
    {
        return base64_encode($this->getImageBytes($upload));
    }

    /**
     * Delete the uploaded image file and mark or delete the metadata record.
     */
    public function deleteUpload(AiImageUpload $upload, bool $force = false): bool
    {
        if (Storage::disk($upload->disk)->exists($upload->path)) {
            Storage::disk($upload->disk)->delete($upload->path);
        }

        $upload->update(['status' => 'deleted']);

        if ($force) {
            return (bool) $upload->forceDelete();
        }

        return (bool) $upload->delete();
    }

    /**
     * Clean up all expired image uploads from storage and database.
     */
    public function cleanupExpiredUploads(): int
    {
        $expired = AiImageUpload::withTrashed()
            ->where(function ($query) {
                $query->where('expires_at', '<=', now())
                    ->orWhere('status', 'deleted');
            })
            ->get();

        $count = 0;
        foreach ($expired as $upload) {
            if (Storage::disk($upload->disk)->exists($upload->path)) {
                Storage::disk($upload->disk)->delete($upload->path);
            }
            $upload->forceDelete();
            $count++;
        }

        return $count;
    }
}
