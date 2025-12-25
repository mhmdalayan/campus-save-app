<?php
require_once 'connection.php';

$email = $_POST['email'];

$stmt = mysqli_prepare($con, "DELETE FROM users WHERE email = ?");
mysqli_stmt_bind_param($stmt, "s", $email);

if(mysqli_stmt_execute($stmt)){
    echo json_encode(array("status" => "success"));
}
else {
    echo json_encode(array("status" => "fail"));
}
?>