<?php

return [

    /*
    |--------------------------------------------------------------------------
    | Third Party Services
    |--------------------------------------------------------------------------
    |
    | This file is for storing the credentials for third party services such
    | as Mailgun, Postmark, AWS and more. This file provides the de facto
    | location for this type of information, allowing packages to have
    | a conventional file to locate the various service credentials.
    |
    */

    'postmark' => [
        'token' => env('POSTMARK_TOKEN'),
    ],

    'ses' => [
        'key' => env('AWS_ACCESS_KEY_ID'),
        'secret' => env('AWS_SECRET_ACCESS_KEY'),
        'region' => env('AWS_DEFAULT_REGION', 'us-east-1'),
    ],

    'slack' => [
        'notifications' => [
            'bot_user_oauth_token' => env('SLACK_BOT_USER_OAUTH_TOKEN'),
            'channel' => env('SLACK_BOT_USER_DEFAULT_CHANNEL'),
        ],
    ],

    'usda' => [
        'api_key' => env('USDA_API_KEY', 'DEMO_KEY'),
        'base_url' => env('USDA_BASE_URL', 'https://api.nal.usda.gov/fdc/v1'),
    ],

    'openfoodfacts' => [
        'base_url' => env('OPENFOODFACTS_BASE_URL', 'https://world.openfoodfacts.org/api/v2'),
        'user_agent' => env('OPENFOODFACTS_USER_AGENT', 'BSNutrition - Android - Version 1.0 - www.bsnutrition.com'),
    ],

];
