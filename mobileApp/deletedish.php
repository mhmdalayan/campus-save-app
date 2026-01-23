<?php
    header('Content-Type: application/json');
    require_once 'connection.php';

    if (!$con) {
        echo json_encode(["success" => false, "message" => "Database connection failed."]);
        exit();
    }

    if (!isset($_POST['id'])) {
        echo json_encode(["success" => false, "message" => "Item ID is missing."]);
        exit();
    }

    $id = $_POST['id'];

    $stmt = mysqli_prepare($con, "DELETE FROM items WHERE id = ?");
    mysqli_stmt_bind_param($stmt, "i", $id);

    if (mysqli_stmt_execute($stmt)) {
        echo json_encode(["success" => true]);
    } else {
        echo json_encode(["success" => false, "message" => "Failed to delete item."]);
    }

    mysqli_stmt_close($stmt);
    mysqli_close($con);
?>