<?php

    header('Access-Control-Allow-Origin: *');
    header('Cache-Control: no-cache');
    
    require '/Slim/Slim.php';
    use Slim\Slim;
    \Slim\Slim::registerAutoloader();

    $app = new Slim();
    $contenttype = $app->request->headers->get('ACCEPT');
    $app->response->headers->set('Content-Type', $contenttype);
    //routing
    $app->get('/area', 'getAreas');
    $app->get('/location', 'getLocations');
    $app->get('/zone', 'getZones');
    $app->get('/event', 'getEvents');
    
    $app->post('/area', 'insertArea');
    $app->post('/zone', 'insertCommand');
    
    $app->put('/area', 'updateArea');
    $app->put('/location', 'updateLocation');
    $app->put('/zone', 'updateZone');
    
    $app->delete('/area/:area',   'deleteArea');

    $app->run();
    
    //-----------------SQL Connection------------------------
    //list of table
    $logintab="user_list";
    $areatab= "arealist";
    $locationtab= "devicelist";
    $statustab= "devicestat";
    $eventtab= "event";
    $commandtab="command";
    
    function dbConnect() { //connect to database
        $url="127.0.0.1";
        $user="root";
        $pass="";
        $dbase="otomasi";
        $dbh = new PDO("mysql:host=$url;dbname=$dbase", $user, $pass);
        $dbh->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
        return $dbh;
    }
    
    //------------------Get Function-------------------------
    function getAreas(){
        $sql = "select * FROM arealist ORDER BY area";
        try {
            $db = dbConnect();
            $stmt = $db->query($sql);
            $areas = $stmt->fetchAll(PDO::FETCH_OBJ);
            $db = null;
            echo json_encode($areas);
        } catch(PDOException $e) {
            echo '{"error":{"text":'. $e->getMessage() .'}}';
        }   
    }
    
    function getLocations(){
        $sql = "SELECT * FROM devicelist ORDER BY location";
        try {
            $db = dbConnect();
            $stmt = $db->query($sql);
            $locs = $stmt->fetchAll(PDO::FETCH_OBJ);
            $db = null;
            echo json_encode($locs);
        } catch(PDOException $e) {
            echo '{"error":{"text":'. $e->getMessage() .'}}';
        }
    }
    
    function getZones(){
        $sql = "SELECT * FROM devicestat ORDER BY address";
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
    
    function getEvents(){
        $sql = "select * FROM event ORDER BY timestamp";
        try {
            $db = dbConnect();
            $stmt = $db->query($sql);
            $events = $stmt->fetchAll(PDO::FETCH_OBJ);
            $db = null;
            echo  json_encode($events);
        } catch(PDOException $e) {
            echo '{"error":{"text":'. $e->getMessage() .'}}';
        }     
    }
    //------------------Put Function-------------------------
    function updateArea(){
        $cmd = Slim::getInstance()->request();
        $sql = "UPDATE arealist SET area=:name WHERE area=:area";
        try {
            $db = dbConnect();
            $stmt = $db->prepare($sql);
            $stmt->bindParam("name", $cmd->put('name'));
            $stmt->bindParam("area", $cmd->put('area'));
            $stmt->execute();
            $db = null;
        } catch(PDOException $e) {
            echo '{"error":{"text":'. $e->getMessage() .'}}';
        } 
    }
    
    function updateLocation(){
        $cmd = Slim::getInstance()->request();
        $sql = "UPDATE devicelist SET location=:location WHERE address=:address";
        try {
            $db = dbConnect();
            $stmt = $db->prepare($sql);
            $stmt->bindParam("location", $cmd->put('location'));
            $stmt->bindParam("address", $cmd->put('address'));
            $stmt->execute();
            $db = null;
        } catch(PDOException $e) {
            echo '{"error":{"text":'. $e->getMessage() .'}}';
        } 
    }
    
    function updateZone(){
        $cmd = Slim::getInstance()->request();
        $sql = "UPDATE devicestat SET alias=:alias WHERE id=:id";
        try {
            $db = dbConnect();
            $stmt = $db->prepare($sql);
            $stmt->bindParam("alias", $cmd->put('alias'));
            $stmt->bindParam("id", $cmd->put('id'));
            $stmt->execute();
            $db = null;
        } catch(PDOException $e) {
            echo '{"error":{"text":'. $e->getMessage() .'}}';
        } 
    }
    
    //------------------Post Function-------------------------
    function insertCommand(){
    //$zone, $address, $mode, $setpoint, $errorband, $lamp
        $cmd = Slim::getInstance()->request();
        $sql = "INSERT INTO command (id, zone, address, mode, setpoint, errorband, lamp) VALUES (default, :zone, :address, :mode, :setpoint, :errorband, :lamp)";
        try {
            $db = dbConnect();
            $stmt = $db->prepare($sql);
            $stmt->bindParam("zone", $cmd->post('zone'));
            $stmt->bindParam("address", $cmd->post('address'));
            $stmt->bindParam("mode", $cmd->post('mode'));
            $stmt->bindParam("setpoint", $cmd->post('setpoint'));
            $stmt->bindParam("errorband", $cmd->post('errorband'));
            $stmt->bindParam("lamp", $cmd->post('lamp'));
            $stmt->execute();
            $db = null;
        } catch(PDOException $e) {
            echo '{"error":{"text":'. $e->getMessage() .'}}';
        } 
    }
    
    function insertArea(){
        $cmd = Slim::getInstance()->request();
        $sql = "INSERT INTO arealist (area) value (:area)";
        try {
            $db = dbConnect();
            $stmt = $db->prepare($sql);
            $stmt->bindParam("area", $cmd->post('area'));
            $stmt->execute();
            $db = null;
        } catch(PDOException $e) {
            echo '{"error":{"text":'. $e->getMessage() .'}}';
        }
    }
    //------------------Delete Function-------------------------
    function deleteArea($area){
        $sql = "DELETE FROM arealist WHERE area=:area";
        try {
            $db = dbConnect();
            $stmt = $db->prepare($sql);
            $stmt->bindParam("area", $area);
            $stmt->execute();
            $db = null;
        } catch(PDOException $e) {
            echo '{"error":{"text":'. $e->getMessage() .'}}';
        }
    }
?>