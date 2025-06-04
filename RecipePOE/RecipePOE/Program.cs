using System;
using System.IO;
using System.Linq;

namespace RecipePOE
{
    internal class Program
    {
        static void Main(string[] args)
        {
            Console.WriteLine("Welcome to Codey Cook Book!");
            Recipe recipe = new Recipe();
            bool exit = false;
            while (!exit)
            {
                Console.WriteLine("\nMenu Options:");
                Console.WriteLine("1. Enter recipe");
                Console.WriteLine("2. Show recipe");
                Console.WriteLine("3. Scale recipe");
                Console.WriteLine("4. Reset quantities");
                Console.WriteLine("5. Clear all input");
                Console.WriteLine("6. Save recipe to file");
                Console.WriteLine("7. Load recipe from file");
                Console.WriteLine("8. Test Load Recipe From File");
                Console.WriteLine("9. Calculate nutritional information");
                Console.WriteLine("10. Exit recipe program");

                int userChoice;
                Console.Write("Enter your choice: ");
                if (!int.TryParse(Console.ReadLine(), out userChoice))
                {
                    Console.WriteLine("Invalid option chosen. Please try again.");
                    continue;
                }

                switch (userChoice)
                {
                    case 1:
                        recipe.InputRecipe();
                        break;
                    case 2:
                        recipe.ShowRecipe();
                        break;
                    case 3:
                        recipe.ScaleRecipe();
                        break;
                    case 4:
                        recipe.ResetRecipeQuantities();
                        break;
                    case 5:
                        recipe.ClearRecipe();
                        break;
                    case 6:
                        recipe.SaveRecipeToFile();
                        break;
                    case 7:
                        recipe.LoadRecipeFromFile();
                        break;
                    case 8:
                        recipe.TestLoadRecipeFromFile();
                        break;
                    case 9:
                        recipe.CalculateNutritionalInfo();
                        break;
                    case 10:
                        exit = true;
                        Console.WriteLine("Exiting the recipe program. Goodbye!");
                        break;
                    default:
                        Console.WriteLine("Invalid option chosen. Please try again.");
                        break;
                }
            }
        }
    }
}

/// <summary>
/// Represents a recipe with ingredients, quantities, units, and steps.
/// </summary>
class Recipe
{
    private const double SCALE_HALF = 0.5;
    private const double SCALE_DOUBLE = 2.0;
    private const double SCALE_TRIPLE = 3.0;

    private string[] ingredient = new string[0];
    private string[] units = new string[0];
    private double[] quantity = new double[0];
    private string[] steps = new string[0];

    /// <summary>
    /// Prompts the user to input the recipe details.
    /// </summary>
    public void InputRecipe()
    {
        int noIngredients = GetNumberOfIngredients();
        InputIngredients(noIngredients);
        int noSteps = GetNumberOfSteps();
        InputSteps(noSteps);
    }

    /// <summary>
    /// Gets the number of ingredients from the user.
    /// </summary>
    /// <returns>The number of ingredients.</returns>
    private int GetNumberOfIngredients()
    {
        int noIngredients;
        Console.WriteLine("Enter number of ingredients in recipe:");
        while (!int.TryParse(Console.ReadLine(), out noIngredients) || noIngredients <= 0)
        {
            Console.WriteLine("Invalid user input. Enter a number > 0:");
        }
        return noIngredients;
    }

    /// <summary>
    /// Inputs the ingredients, quantities, and units from the user.
    /// </summary>
    /// <param name="noIngredients">The number of ingredients to input.</param>
    private void InputIngredients(int noIngredients)
    {
        Array.Resize(ref ingredient, noIngredients);
        Array.Resize(ref quantity, noIngredients);
        Array.Resize(ref units, noIngredients);

        for (int a = 0; a < noIngredients; a++)
        {
            Console.WriteLine($"Enter the name of ingredient {a + 1} (e.g., flour, sugar):");
            ingredient[a] = Console.ReadLine();
            while (string.IsNullOrWhiteSpace(ingredient[a]))
            {
                Console.WriteLine("Ingredient name cannot be empty. Please enter a valid name:");
                ingredient[a] = Console.ReadLine();
            }

            Console.WriteLine($"Enter the quantity of {ingredient[a]} (e.g., 2.5):");
            double quantities;
            while (!double.TryParse(Console.ReadLine(), out quantities) || quantities <= 0)
            {
                Console.WriteLine("Invalid user input. Enter a number > 0:");
            }
            quantity[a] = quantities;

            Console.WriteLine($"Enter unit of measurement for {ingredient[a]} (e.g., cups, grams):");
            units[a] = Console.ReadLine();
            while (string.IsNullOrWhiteSpace(units[a]))
            {
                Console.WriteLine("Unit of measurement cannot be empty. Please enter a valid unit:");
                units[a] = Console.ReadLine();
            }
        }
    }

    /// <summary>
    /// Gets the number of steps from the user.
    /// </summary>
    /// <returns>The number of steps.</returns>
    private int GetNumberOfSteps()
    {
        int noSteps;
        Console.WriteLine("Enter the number of steps to complete recipe:");
        while (!int.TryParse(Console.ReadLine(), out noSteps) || noSteps <= 0)
        {
            Console.WriteLine("Invalid user input. Enter a number > 0:");
        }
        return noSteps;
    }

    /// <summary>
    /// Inputs the steps from the user.
    /// </summary>
    /// <param name="noSteps">The number of steps to input.</param>
    private void InputSteps(int noSteps)
    {
        Array.Resize(ref steps, noSteps);
        for (int b = 0; b < noSteps; b++)
        {
            Console.WriteLine($"Enter step {b + 1} (e.g., Mix ingredients, Bake for 30 minutes):");
            steps[b] = Console.ReadLine();
            while (string.IsNullOrWhiteSpace(steps[b]))
            {
                Console.WriteLine("Step cannot be empty. Please enter a valid step:");
                steps[b] = Console.ReadLine();
            }
        }
    }

    /// <summary>
    /// Displays the recipe details.
    /// </summary>
    public void ShowRecipe()
    {
        Console.WriteLine("Recipe:");
        Console.WriteLine("Ingredients");

        for (int i = 0; i < ingredient.Length; i++)
        {
            Console.WriteLine($"{quantity[i]} of {units[i]} {ingredient[i]}");
        }
        Console.WriteLine("Steps to complete:");

        for (int j = 0; j < steps.Length; j++)
        {
            Console.WriteLine($"{j + 1}. {steps[j]}");
        }
    }

    /// <summary>
    /// Scales the recipe quantities by a specified factor.
    /// </summary>
    public void ScaleRecipe()
    {
        double scaling;

        Console.WriteLine("Enter preferred scale for ingredient. Options (0.5, 2, and 3):");
        if (!double.TryParse(Console.ReadLine(), out scaling) || (scaling != SCALE_HALF && scaling != SCALE_DOUBLE && scaling != SCALE_TRIPLE))
        {
            Console.WriteLine("Error scaling number. Enter value 0.5, 2, or 3");
            return;
        }

        for (int i = 0; i < quantity.Length; i++)
        {
            quantity[i] *= scaling;
        }
        Console.WriteLine($"Recipe has been scaled by {scaling}.");
    }

    /// <summary>
    /// Resets the quantities of the recipe to zero.
    /// </summary>
    public void ResetRecipeQuantities()
    {
        for (int i = 0; i < quantity.Length; i++)
        {
            quantity[i] = 0;
        }
        Console.WriteLine("Recipe quantities have been reset.");
    }

    /// <summary>
    /// Clears all data from the recipe.
    /// </summary>
    public void ClearRecipe()
    {
        Array.Resize(ref ingredient, 0);
        Array.Resize(ref quantity, 0);
        Array.Resize(ref units, 0);
        Array.Resize(ref steps, 0);
        Console.WriteLine("Recipe has been cleared.");
    }

    /// <summary>
    /// Saves the recipe to a text file.
    /// </summary>
    public void SaveRecipeToFile()
    {
        Console.WriteLine("Enter the filename to save the recipe (e.g., my_recipe.txt):");
        string filename = Console.ReadLine();
        if (string.IsNullOrWhiteSpace(filename))
        {
            Console.WriteLine("Filename cannot be empty. Please enter a valid filename.");
            return;
        }

        using (StreamWriter writer = new StreamWriter(filename))
        {
            writer.WriteLine("Recipe:");
            writer.WriteLine("Ingredients");
            for (int i = 0; i < ingredient.Length; i++)
            {
                writer.WriteLine($"{quantity[i]} of {units[i]} {ingredient[i]}");
            }
            writer.WriteLine("Steps to complete:");
            for (int j = 0; j < steps.Length; j++)
            {
                writer.WriteLine($"{j + 1}. {steps[j]}");
            }
        }
        Console.WriteLine($"Recipe has been saved to {filename}.");
    }

    /// <summary>
    /// Loads a recipe from a text file.
    /// </summary>
    public void LoadRecipeFromFile()
    {
        Console.WriteLine("Enter the filename to load the recipe (e.g., my_recipe.txt):");
        string filename = Console.ReadLine();
        if (string.IsNullOrWhiteSpace(filename))
        {
            Console.WriteLine("Filename cannot be empty. Please enter a valid filename.");
            return;
        }

        if (!File.Exists(filename))
        {
            Console.WriteLine("File does not exist. Please check the filename and try again.");
            return;
        }

        using (StreamReader reader = new StreamReader(filename))
        {
            string line;
            bool readingIngredients = false;
            bool readingSteps = false;
            int ingredientIndex = 0;
            int stepIndex = 0;

            while ((line = reader.ReadLine()) != null)
            {
                if (line == "Ingredients")
                {
                    readingIngredients = true;
                    readingSteps = false;
                    continue;
                }
                else if (line == "Steps to complete:")
                {
                    readingIngredients = false;
                    readingSteps = true;
                    continue;
                }

                if (readingIngredients)
                {
                    string[] parts = line.Split(new[] { ' ' }, StringSplitOptions.RemoveEmptyEntries);
                    if (parts.Length >= 3)
                    {
                        double quantity;
                        if (double.TryParse(parts[0], out quantity))
                        {
                            Array.Resize(ref ingredient, ingredientIndex + 1);
                            Array.Resize(ref this.quantity, ingredientIndex + 1);
                            Array.Resize(ref units, ingredientIndex + 1);

                            this.quantity[ingredientIndex] = quantity;
                            units[ingredientIndex] = parts[1];
                            ingredient[ingredientIndex] = string.Join(" ", parts.Skip(2));
                            ingredientIndex++;
                        }
                    }
                }
                else if (readingSteps)
                {
                    Array.Resize(ref steps, stepIndex + 1);
                    steps[stepIndex] = line;
                    stepIndex++;
                }
            }
        }
        Console.WriteLine("Recipe has been loaded successfully.");
    }

    /// <summary>
    /// Unit test for the LoadRecipeFromFile method.
    /// </summary>
    public void TestLoadRecipeFromFile()
    {
        // Create a test file with a known recipe
        string testFilename = "test_recipe.txt";
        using (StreamWriter writer = new StreamWriter(testFilename))
        {
            writer.WriteLine("Recipe:");
            writer.WriteLine("Ingredients");
            writer.WriteLine("2 cups flour");
            writer.WriteLine("1 cup sugar");
            writer.WriteLine("Steps to complete:");
            writer.WriteLine("1. Mix ingredients");
            writer.WriteLine("2. Bake for 30 minutes");
        }

        // Load the recipe from the test file
        LoadRecipeFromFile();

        // Verify the loaded recipe
        Console.WriteLine("Testing LoadRecipeFromFile:");
        Console.WriteLine("Ingredients loaded:");
        for (int i = 0; i < ingredient.Length; i++)
        {
            Console.WriteLine($"{quantity[i]} of {units[i]} {ingredient[i]}");
        }
        Console.WriteLine("Steps loaded:");
        for (int j = 0; j < steps.Length; j++)
        {
            Console.WriteLine($"{j + 1}. {steps[j]}");
        }

        // Clean up the test file
        File.Delete(testFilename);
    }

    /// <summary>
    /// Calculates and displays nutritional information for the recipe.
    /// </summary>
    public void CalculateNutritionalInfo()
    {
        Console.WriteLine("Calculating nutritional information for the recipe...");
        double totalCalories = 0;
        double totalProtein = 0;
        double totalCarbs = 0;
        double totalFat = 0;

        for (int i = 0; i < ingredient.Length; i++)
        {
            // Example nutritional values (in grams per 100g of ingredient)
            double calories = 0;
            double protein = 0;
            double carbs = 0;
            double fat = 0;

            // This is a placeholder for actual nutritional data retrieval
            // In a real application, you would look up the nutritional values for each ingredient
            // For now, we will use dummy values
            switch (ingredient[i].ToLower())
            {
                case "flour":
                    calories = 364;
                    protein = 10;
                    carbs = 76;
                    fat = 1;
                    break;
                case "sugar":
                    calories = 387;
                    protein = 0;
                    carbs = 100;
                    fat = 0;
                    break;
                // Add more ingredients as needed
                default:
                    Console.WriteLine($"No nutritional data available for {ingredient[i]}.");
                    continue;
            }

            // Calculate nutritional values based on quantity
            totalCalories += (calories * quantity[i]) / 100;
            totalProtein += (protein * quantity[i]) / 100;
            totalCarbs += (carbs * quantity[i]) / 100;
            totalFat += (fat * quantity[i]) / 100;
        }

        Console.WriteLine("Nutritional Information:");
        Console.WriteLine($"Total Calories: {totalCalories:F2} kcal");
        Console.WriteLine($"Total Protein: {totalProtein:F2} g");
        Console.WriteLine($"Total Carbohydrates: {totalCarbs:F2} g");
        Console.WriteLine($"Total Fat: {totalFat:F2} g");
    }
}
