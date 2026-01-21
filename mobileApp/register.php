<?php
header('Content-Type: application/json');
require_once 'connection.php';

if (!$con) {
    echo json_encode(["success" => false, "message" => "Database connection failed."]);
    exit();
}

mysqli_set_charset($con, 'utf8mb4');

if (!isset($_POST['email']) || !isset($_POST['username']) || !isset($_POST['password'])) {
    echo json_encode(["success" => false, "message" => "Email, username, and password are required."]);
    exit();
}

$email = trim($_POST['email']);
$username = trim($_POST['username']);
$password = $_POST['password'];

if (empty($email) || empty($username) || empty($password)) {
    echo json_encode(["success" => false, "message" => "Email, username, and password cannot be empty."]);
    exit();
}

// Check if email or username already exists
$checkStmt = mysqli_prepare($con, "SELECT id FROM users WHERE email = ? OR username = ?");
if ($checkStmt === false) {
    echo json_encode(["success" => false, "message" => "Database check preparation failed."]);
    exit();
}
mysqli_stmt_bind_param($checkStmt, "ss", $email, $username);
mysqli_stmt_execute($checkStmt);
$result = mysqli_stmt_get_result($checkStmt);
if (mysqli_num_rows($result) > 0) {
    echo json_encode(["success" => false, "message" => "Email or username already exists."]);
    mysqli_stmt_close($checkStmt);
    exit();
}
mysqli_stmt_close($checkStmt);

// Hash the password
$hashed_password = password_hash($password, PASSWORD_DEFAULT);

// Insert the new user
$insertStmt = mysqli_prepare($con, "INSERT INTO users (email, username, password) VALUES (?, ?, ?)");
if ($insertStmt === false) {
    echo json_encode(["success" => false, "message" => "Database insert preparation failed."]);
    exit();
}
mysqli_stmt_bind_param($insertStmt, "sss", $email, $username, $hashed_password);
$success = mysqli_stmt_execute($insertStmt);

if ($success) {
    echo json_encode(["success" => true, "message" => "Registration successful."]);
} else {
    echo json_encode(["success" => false, "message" => "Registration failed."]);
}

mysqli_stmt_close($insertStmt);
mysqli_close($con);
?>