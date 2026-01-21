<?php
error_reporting(E_ALL);
ini_set('display_errors', 0); // Don't display errors in output
header('Content-Type: application/json');
require_once 'connection.php';

if (!$con) {
    echo json_encode(["success" => false, "message" => "Database connection failed: " . mysqli_connect_error()]);
    exit();
}

if (!isset($_POST['username']) || !isset($_FILES['profile_pic'])) {
    echo json_encode(["success" => false, "message" => "Username or profile picture not provided."]);
    exit();
}

$username = trim($_POST['username']); // Trim any whitespace
$image = $_FILES['profile_pic'];

if ($image['error'] !== UPLOAD_ERR_OK) {
    $errorMessages = [
        UPLOAD_ERR_INI_SIZE => "The uploaded file exceeds the upload_max_filesize directive in php.ini.",
        UPLOAD_ERR_FORM_SIZE => "The uploaded file exceeds the MAX_FILE_SIZE directive that was specified in the HTML form.",
        UPLOAD_ERR_PARTIAL => "The uploaded file was only partially uploaded.",
        UPLOAD_ERR_NO_FILE => "No file was uploaded.",
        UPLOAD_ERR_NO_TMP_DIR => "Missing a temporary folder.",
        UPLOAD_ERR_CANT_WRITE => "Failed to write file to disk.",
        UPLOAD_ERR_EXTENSION => "A PHP extension stopped the file upload."
    ];
    $message = isset($errorMessages[$image['error']]) ? $errorMessages[$image['error']] : "Unknown upload error.";
    echo json_encode(["success" => false, "message" => "File upload error: " . $message]);
    exit();
}

// First, check if user exists
$checkStmt = mysqli_prepare($con, "SELECT username FROM users WHERE username = ?");
if ($checkStmt === false) {
    echo json_encode(["success" => false, "message" => "Database check statement preparation failed: " . mysqli_error($con)]);
    exit();
}
mysqli_stmt_bind_param($checkStmt, "s", $username);
mysqli_stmt_execute($checkStmt);
$result = mysqli_stmt_get_result($checkStmt);
if (mysqli_num_rows($result) == 0) {
    echo json_encode(["success" => false, "message" => "No user found with that username: " . $username]);
    mysqli_stmt_close($checkStmt);
    exit();
}
mysqli_stmt_close($checkStmt);

// Read the image data
$imageData = file_get_contents($image['tmp_name']);

if ($imageData === false) {
    echo json_encode(["success" => false, "message" => "Failed to read image data."]);
    exit();
}

// Prepare statement to update profile picture
$stmt = mysqli_prepare($con, "UPDATE users SET profile_picture = ? WHERE username = ?");
if ($stmt === false) {
    echo json_encode(["success" => false, "message" => "Database statement preparation failed: " . mysqli_error($con)]);
    exit();
}

mysqli_stmt_bind_param($stmt, "bs", $imageData, $username);
mysqli_stmt_send_long_data($stmt, 0, $imageData); // For large data
$success = mysqli_stmt_execute($stmt);

if ($success) {
    $affected = mysqli_stmt_affected_rows($stmt);
    if ($affected > 0) {
        echo json_encode(["success" => true, "message" => "Profile picture updated successfully."]);
    } else {
        echo json_encode(["success" => false, "message" => "No user found with that username: " . $username]);
    }
} else {
    echo json_encode(["success" => false, "message" => "Failed to update profile picture: " . mysqli_stmt_error($stmt)]);
}

mysqli_stmt_close($stmt);
mysqli_close($con);
?>