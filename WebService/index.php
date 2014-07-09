<?php

    header('Access-Control-Allow-Origin: *');
    header('Cache-Control: no-cache');
    
    require '/Slim/Slim.php';
    use Slim\Slim;
    \Slim\Slim::registerAutoloader();

    $app = new Slim();
    $contenttype = $app->request->headers->get('ACCEPT');
    $app->response->headers->set('Content-Type', $contenttype);
    //-------------------------routing-----------------------------
    $app->get('/status', 'getStatus'); //get list of are group
    $app->post('/command', 'insertCommand'); //insert command for controlling device

    $app->run();
    
    //-----------------SQL Connection------------------------

    function dbConnect() { //connect to database
        $url="127.0.0.1";
        $user="root";
        $pass="root";
        $dbase="otomasi";
        $dbh = new PDO("mysql:host=$url;dbname=$dbase", $user, $pass);
        $dbh->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
        return $dbh;
    }
    
    //------------------Get Function-------------------------  
    function getStatus(){
        $sql = "SELECT * FROM status ORDER BY address";
        try {
            $db = dbConnect();
            $stmt = $db->query($sql);
            $stats = $stmt->fetchAll(PDO::FETCH_OBJ);
            $db = null;
            echo json_encode($stats);
        } catch(PDOException $e) {
            echo '{"error":{"text":'. $e->getMessage() .'}}';
        }
    }
       
    //------------------Post Function-------------------------
    function insertCommand(){
    //$zone, $address, $mode, $setpoint, $errorband, $lamp
        $cmd = Slim::getInstance()->request();
        $sql = "INSERT INTO command (address, amode, lmode, arel, lrel, aset, aerror, lset, lerror) VALUES (default, :address, :amode, :lmode, :arel, :lrel, :aset, :aerror, :lset, :lerror)";
        $sql2 = "UPDATE status SET amode=:amode, lmode=:lmode, arel=:arel, lrel=:lrel, aset=:aset, aerror=:aerror, lset=:lerror, lerror=:lerror WHERE address=:address";
        try {
            $db = dbConnect();
            $stmt = $db->prepare($sql);
            $stmt2 = $db->prepare($sql2);
            
            $stmt->bindParam("address", $cmd->post('address'));
            $stmt->bindParam("amode", $cmd->post('amode'));
            $stmt->bindParam("lmode", $cmd->post('lmode'));
            $stmt->bindParam("arel", $cmd->post('arel'));
            $stmt->bindParam("lrel", $cmd->post('lrel'));
            $stmt->bindParam("aset", $cmd->post('aset'));
            $stmt->bindParam("lset", $cmd->post('lset'));
            $stmt->bindParam("aerror", $cmd->post('aerror'));
            $stmt->bindParam("lerror", $cmd->post('lerror'));
            
            $stmt->bindParam("address", $cmd->post('address'));
            $stmt->bindParam("amode", $cmd->post('amode'));
            $stmt->bindParam("lmode", $cmd->post('lmode'));
            $stmt->bindParam("arel", $cmd->post('arel'));
            $stmt->bindParam("lrel", $cmd->post('lrel'));
            $stmt->bindParam("aset", $cmd->post('aset'));
            $stmt->bindParam("lset", $cmd->post('lset'));
            $stmt->bindParam("aerror", $cmd->post('aerror'));
            $stmt->bindParam("lerror", $cmd->post('lerror'));
            
            $stmt2->execute();
            $stmt->execute();
            $db = null;
        } catch(PDOException $e) {
            echo '{"error":{"text":'. $e->getMessage() .'}}';
        } 
    }
?>