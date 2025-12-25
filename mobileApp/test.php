<?php
    // Use the full, standard PHP opening tag.

    // Set the header to guarantee JSON content type.header('Content-Type: application/json');

    // Create a simple PHP array.
    $response_data = [
        "success" => true,
        "message" => "Connection to test.php was successful!"
    ];

    // Encode the array into a JSON string and echo it.
    echo json_encode($response_data);

    // Stop the script to ensure no other output is sent.
    exit();
?>
