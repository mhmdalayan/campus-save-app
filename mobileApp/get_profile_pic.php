<?php
require_once 'connection.php';

if (!$con) {
    http_response_code(500);
    exit();
}

if (!isset($_GET['username'])) {
    http_response_code(400);
    exit();
}

$username = strtolower(trim($_GET['username']));

$stmt = mysqli_prepare($con, "SELECT profile_picture FROM users WHERE LOWER(username) = ?");
if ($stmt === false) {
    http_response_code(500);
    exit();
}

mysqli_stmt_bind_param($stmt, "s", $username);
mysqli_stmt_execute($stmt);
$result = mysqli_stmt_get_result($stmt);

if (mysqli_num_rows($result) == 1) {
    $row = mysqli_fetch_assoc($result);
    $imageData = $row['profile_picture'];
    if ($imageData) {
        error_log("Image data length: " . strlen($imageData));
        header('Content-Type: image/jpeg'); // Assuming JPEG, adjust if needed
        echo $imageData;
    } else {
        http_response_code(404); // No image
    }
} else {
    http_response_code(404);
}

mysqli_stmt_close($stmt);
mysqli_close($con);
?>