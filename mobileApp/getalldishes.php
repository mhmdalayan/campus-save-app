<?php
require_once 'connection.php';

// Login User
$email = $_POST['email'];
$password = $_POST['password'];

$stmt = mysqli_prepare($con, "SELECT password FROM users WHERE email = ?");
mysqli_stmt_bind_param($stmt, "s", $email);
mysqli_stmt_execute($stmt);
$result = mysqli_stmt_get_result($stmt);

if ($row = mysqli_fetch_assoc($result)) {
    if (password_verify($password, $row['password'])) {
        echo json_encode(array("status" => "success"));
    } else {
        echo json_encode(array("status" => "fail", "message" => "Invalid password"));
    }
} else {
    echo json_encode(array("status" => "fail", "message" => "User not found"));
}
?>