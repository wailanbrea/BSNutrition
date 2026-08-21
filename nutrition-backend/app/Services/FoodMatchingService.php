<?php

namespace App\Services;

use App\DTOs\FoodMatchCandidate;
use App\Models\Food;
use App\Models\FoodAlias;
use Illuminate\Support\Str;

class FoodMatchingService
{
    private const DIRECT_MATCH_THRESHOLD = 0.70;

    /**
     * Attempt to match a recognized food string directly with a single high-confidence canonical food.
     */
    public function matchFoodName(
        string $name,
        ?string $preparation = null,
        string $locale = 'DO',
        float $minThreshold = self::DIRECT_MATCH_THRESHOLD
    ): ?FoodMatchCandidate {
        $candidates = $this->findCandidates($name, $preparation, $locale, 1);

        if (! empty($candidates) && $candidates[0]->score >= $minThreshold) {
            return $candidates[0];
        }

        return null;
    }

    /**
     * Find candidate matches ranked by relevance score for user review.
     *
     * @return array<int, FoodMatchCandidate>
     */
    public function findCandidates(
        string $name,
        ?string $preparation = null,
        string $locale = 'DO',
        int $limit = 5
    ): array {
        $cleanQuery = trim($name);
        if ($cleanQuery === '') {
            return [];
        }

        $normalizedQuery = $this->normalize($cleanQuery);
        $candidates = [];
        $seenFoodIds = [];

        // 1. Exact match on canonical_name
        $exactFoods = Food::query()
            ->with(['brand', 'category', 'foodNutrients.nutrient', 'portions'])
            ->whereRaw('LOWER(canonical_name) = ?', [strtolower($cleanQuery)])
            ->get();

        foreach ($exactFoods as $food) {
            $candidates[] = new FoodMatchCandidate($food, 1.0, 'exact');
            $seenFoodIds[$food->id] = true;
        }

        // 2. Exact match on food_aliases
        $exactAliases = FoodAlias::query()
            ->with(['food.brand', 'food.category', 'food.foodNutrients.nutrient', 'food.portions'])
            ->whereRaw('LOWER(alias) = ?', [strtolower($cleanQuery)])
            ->get();

        foreach ($exactAliases as $aliasRecord) {
            if (! isset($seenFoodIds[$aliasRecord->food_id]) && $aliasRecord->food) {
                $candidates[] = new FoodMatchCandidate(
                    $aliasRecord->food,
                    0.95,
                    'alias',
                    $aliasRecord->alias
                );
                $seenFoodIds[$aliasRecord->food_id] = true;
            }
        }

        // 3. Search query match (search scope)
        $searchResults = Food::query()
            ->with(['brand', 'category', 'foodNutrients.nutrient', 'portions', 'aliases'])
            ->search($cleanQuery)
            ->limit(20)
            ->get();

        foreach ($searchResults as $food) {
            if (isset($seenFoodIds[$food->id])) {
                continue;
            }

            $score = $this->calculateSimilarityScore($food, $normalizedQuery, $preparation, $locale);
            if ($score >= 0.40) {
                $candidates[] = new FoodMatchCandidate($food, $score, 'token');
                $seenFoodIds[$food->id] = true;
            }
        }

        // Sort descending by score
        usort($candidates, fn (FoodMatchCandidate $a, FoodMatchCandidate $b) => $b->score <=> $a->score);

        return array_slice($candidates, 0, $limit);
    }

    /**
     * Calculate similarity score between food item and query.
     */
    public function calculateSimilarityScore(
        Food $food,
        string $normalizedQuery,
        ?string $preparation = null,
        string $locale = 'DO'
    ): float {
        $normalizedCanonical = $this->normalize($food->canonical_name);

        // Exact normalized match
        if ($normalizedCanonical === $normalizedQuery) {
            return 0.98;
        }

        // Starts with or contains
        if (str_starts_with($normalizedCanonical, $normalizedQuery)) {
            $baseScore = 0.90;
        } elseif (str_contains($normalizedCanonical, $normalizedQuery) || str_contains($normalizedQuery, $normalizedCanonical)) {
            $baseScore = 0.85;
        } else {
            // Levenshtein & similar_text
            similar_text($normalizedCanonical, $normalizedQuery, $percent);
            $baseScore = $percent / 100.0 * 0.85;

            // Check aliases
            foreach ($food->aliases as $alias) {
                $normalizedAlias = $this->normalize($alias->alias);
                if ($normalizedAlias === $normalizedQuery) {
                    $baseScore = max($baseScore, 0.94);
                } elseif (str_contains($normalizedAlias, $normalizedQuery)) {
                    $baseScore = max($baseScore, 0.88);
                }
            }
        }

        // Locale bonus (Dominican / Caribbean boost)
        if ($food->country_code === $locale) {
            $baseScore += 0.05;
        }

        // Verified bonus
        if ($food->verified) {
            $baseScore += 0.03;
        }

        // Preparation method bonus if matching
        if ($preparation !== null && ! empty($preparation)) {
            $normalizedPrep = $this->normalize($preparation);
            if (str_contains($normalizedCanonical, $normalizedPrep)) {
                $baseScore += 0.05;
            }
        }

        return min(1.0, max(0.0, $baseScore));
    }

    /**
     * Normalize string by lowercasing, stripping accents, and removing non-alphanumeric chars.
     */
    public function normalize(string $string): string
    {
        $string = mb_strtolower($string, 'UTF-8');
        $string = Str::ascii($string);
        $string = preg_replace('/[^a-z0-9\s]/', '', $string) ?? '';

        return trim(preg_replace('/\s+/', ' ', $string) ?? '');
    }
}
