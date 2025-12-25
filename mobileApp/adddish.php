<?php
    // C:\xampp\htdocs\mobileApp\adddish.php
    header('Content-Type: application/json');
    require_once 'connection.php';

    if (!$con) {
        echo json_encode(["success" => false, "message" => "Database connection failed."]);
        exit();
    }

    // Check if the app sent all required parameters, including the new username.
    if (!isset($_POST['email']) || !isset($_POST['username']) || !isset($_POST['password'])) {
        echo json_encode(["success" => false, "message" => "Required parameters not provided by the app."]);
        exit();
    }

    $email = $_POST['email'];
    $username = $_POST['username']; // <-- Get username
    $password = $_POST['password'];

    $hashed_password = password_hash($password, PASSWORD_DEFAULT);

    // Update the SQL query to include the username.
    $stmt = mysqli_prepare($con, "INSERT INTO users(email, username, password) VALUES (?, ?, ?)");

    if ($stmt === false) {
        echo json_encode(["success" => false, "message" => "Database statement preparation failed."]);
        exit();
    }

    // Bind parameters to the statement. The type string is now "sss" (string, string, string).
    mysqli_stmt_bind_param($stmt, "sss",  $email, $username, $hashed_password);

    if (mysqli_stmt_execute($stmt)) {
        echo json_encode(["success" => true]);
    } else {
        // Check for specific duplicate errors. MySQL error 1062 is for any UNIQUE key.
        if (mysqli_errno($con) == 1062) {
            // Check if the error message contains 'email' to give a specific message.
            if (strpos(mysqli_error($con), 'email') !== false) {
                echo json_encode(["success" => false, "message" => "This email is already registered."]);
            }
            // Check if the error message contains 'username'.
            else if (strpos(mysqli_error($con), 'username') !== false) {
                echo json_encode(["success" => false, "message" => "This username is already taken."]);
            }
            else {
                echo json_encode(["success" => false, "message" => "A unique field is already in use."]);
            }
        } else {
            $error_message = mysqli_stmt_error($stmt);
            echo json_encode(["success" => false, "message" => "Registration failed: " . $error_message]);
        }
    }

    mysqli_stmt_close($stmt);
    mysqli_close($con);
    exit();
?>
