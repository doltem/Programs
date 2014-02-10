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
    $app->get('/area', 'getAreas'); //get list of are group
    $app->get('/location', 'getLocations'); //get list of devices
    $app->get('/zone', 'getZones'); //get list of zone
    $app->get('/event', 'getEvents'); //get list of events
    $app->get('/schedule', 'getSchedules'); //get list of schedule
    
    $app->post('/area', 'insertArea'); //insert group
    $app->post('/zone', 'insertCommand'); //insert command for controlling device
    $app->post('/schedule', 'insertSchedule'); //insert schedule
    
    $app->put('/area', 'updateArea');
    $app->put('/location', 'updateLocation');
    $app->put('/zone', 'updateZone');
    
    $app->delete('/area/:area',   'deleteArea');
    $app->post('/schedule/delete',   'deleteSchedule');

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
        $sql = "select * FROM event WHERE DATE(timestamp) = CURDATE() ORDER BY timestamp";
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
    
    function getSchedules(){
        $sql = "select * FROM schedule";
        try {
            $db = dbConnect();
            $stmt = $db->query($sql);
            $schedules = $stmt->fetchAll(PDO::FETCH_OBJ);
            $db = null;
            echo  json_encode($schedules);
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
        $sql2 = "UPDATE devicestat SET mode=:mode, setpoint=:setpoint, errorband=:errorband, lamp=:lamp WHERE address=:address AND zone=:zone";
        try {
            $db = dbConnect();
            $stmt = $db->prepare($sql);
            $stmt2 = $db->prepare($sql2);
            
            $stmt->bindParam("zone", $cmd->post('zone'));
            $stmt->bindParam("address", $cmd->post('address'));
            $stmt->bindParam("mode", $cmd->post('mode'));
            $stmt->bindParam("setpoint", $cmd->post('setpoint'));
            $stmt->bindParam("errorband", $cmd->post('errorband'));
            $stmt->bindParam("lamp", $cmd->post('lamp'));
            
            $stmt2->bindParam("zone", $cmd->post('zone'));
            $stmt2->bindParam("address", $cmd->post('address'));
            $stmt2->bindParam("mode", $cmd->post('mode'));
            $stmt2->bindParam("setpoint", $cmd->post('setpoint'));
            $stmt2->bindParam("errorband", $cmd->post('errorband'));
            $stmt2->bindParam("lamp", $cmd->post('lamp'));
            
            
            $stmt->execute();
            $stmt2->execute();
            $db = null;
        } catch(PDOException $e) {
            echo '{"error":{"text":'. $e->getMessage() .'}}';
        } 
    }
    
    function insertSchedule(){
        $cmd = Slim::getInstance()->request();
        $sql = "INSERT INTO schedule (id, dstart, dend, tstart, tend, address, zone, lamp, mode, active) VALUES (default, :dstart, :dend, :tstart, :tend, :address, :zone, :lamp, :mode, default)";
        try {
            $db = dbConnect();
            $stmt = $db->prepare($sql);
            
            $stmt->bindParam("dstart", $cmd->post('dstart'));
            $stmt->bindParam("dend", $cmd->post('dend'));
            $stmt->bindParam("tstart", $cmd->post('tstart'));
            $stmt->bindParam("tend", $cmd->post('tend'));
            $stmt->bindParam("address", $cmd->post('address'));
            $stmt->bindParam("zone", $cmd->post('zone'));          
            $stmt->bindParam("lamp", $cmd->post('lamp'));
            $stmt->bindParam("mode", $cmd->post('mode'));
            
            
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
    
    function deleteSchedule(){
        $cmd = Slim::getInstance()->request();
        $sql = "DELETE FROM schedule WHERE id=:id";
        try {
            $db = dbConnect();
            $stmt = $db->prepare($sql);
            $stmt->bindParam("id", $cmd->post('id'));
            $stmt->execute();
            $db = null;
        } catch(PDOException $e) {
            echo '{"error":{"text":'. $e->getMessage() .'}}';
        }
    }
?>