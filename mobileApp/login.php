<?php
    header('Content-Type: application/json');
    require_once 'connection.php'; // Your existing silent connection script

    // Check for a database connection failure first.
    if (!$con) {
        echo json_encode(["success" => false, "message" => "Database connection failed."]);
        exit();
    }

    // Check if the app sent the required parameters.
    if (!isset($_POST['email']) || !isset($_POST['password'])) {
        echo json_encode(["success" => false, "message" => "Email or password not provided."]);
        exit();
    }

    $email = $_POST['email'];
    $password = $_POST['password'];

    // 1. Prepare a statement to find the user by email
    $stmt = mysqli_prepare($con, "SELECT username, password FROM users WHERE email = ?");
    if ($stmt === false) {
        echo json_encode(["success" => false, "message" => "Database statement preparation failed."]);
        exit();
    }

    mysqli_stmt_bind_param($stmt, "s", $email);
    mysqli_stmt_execute($stmt);
    $result = mysqli_stmt_get_result($stmt);

    // 2. Check if a user with that email was found
    if (mysqli_num_rows($result) == 1) {
        $user = mysqli_fetch_assoc($result);
        $hashed_password = $user['password'];

        // 3. Verify the provided password against the stored hash
        if (password_verify($password, $hashed_password)) {
            // Password is correct! Send a success response.
            // You can also send back user data like the username.
            echo json_encode([
                "success" => true,
                "username" => $user['username'] // Sending username back is a nice touch
            ]);
        } else {
            // Password is incorrect
            echo json_encode(["success" => false, "message" => "Invalid email or password."]);
        }
    } else {
        // No user found with that email
        echo json_encode(["success" => false, "message" => "Invalid email or password."]);
    }

    // Clean up
    mysqli_stmt_close($stmt);
    mysqli_close($con);
    exit();
?>
