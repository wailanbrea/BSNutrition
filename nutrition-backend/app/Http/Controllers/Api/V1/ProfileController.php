<?php

namespace App\Http\Controllers\Api\V1;

use App\Http\Controllers\Controller;
use App\Http\Requests\Profile\UpdateProfileRequest;
use App\Http\Resources\UserProfileResource;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;

class ProfileController extends Controller
{
    public function show(Request $request): JsonResponse
    {
        $user = $request->user();
        $profile = $user->profile ?? $user->profile()->create([]);

        return response()->json([
            'profile' => new UserProfileResource($profile),
        ]);
    }

    public function update(UpdateProfileRequest $request): JsonResponse
    {
        $user = $request->user();
        $profile = $user->profile()->updateOrCreate(
            ['user_id' => $user->id],
            $request->validated()
        );

        return response()->json([
            'profile' => new UserProfileResource($profile),
        ]);
    }
}
