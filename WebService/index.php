<?php
    require '/Slim/Slim.php';
    use Slim\Slim;
    \Slim\Slim::registerAutoloader();

    $app = new Slim();
    
    //routing
    $app->get('/hello/:name', function ($name) {
        echo "Hello, $name";
    });
    
    $app->get('/area', 'getAreas');
    $app->get('/location/:area', 'getLocations');
    $app->get('/zone/:address', 'getZones');
    $app->get('/event', 'getEvents');
    
    $app->post('/area/:area', 'insertArea');
    $app->post('/command', 'insertCommand');
    
    $app->put('/area/:name/:area', 'updateArea');
    $app->put('/location/:name/:address', 'updateLocation');
    $app->put('/zone/:name/:id', 'updateZone');
    
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
            echo '{"areas": ' . json_encode($areas) . '}';
        } catch(PDOException $e) {
            echo '{"error":{"text":'. $e->getMessage() .'}}';
        }   
    }
    
    function getLocations($area){
        global $locationtab;
        $sql = "SELECT * FROM devicelist WHERE area=:area";
        try {
            $db = dbConnect();
            $stmt = $db->prepare($sql);
            $stmt->bindParam("area", $area);
            $stmt->execute();
            $locs = $stmt->fetchAll(PDO::FETCH_OBJ);
            $db = null;
            echo '{"locations": ' . json_encode($locs) . '}';
        } catch(PDOException $e) {
            echo '{"error":{"text":'. $e->getMessage() .'}}';
        }
    }
    
    function getZones($address){
        $sql = "SELECT * FROM devicestat WHERE address=:address";
        try {
            $db = dbConnect();
            $stmt = $db->prepare($sql);
            $stmt->bindParam("address", $address);
            $stmt->execute();
            $stats = $stmt->fetchAll(PDO::FETCH_OBJ);
            $db = null;
            echo '{"stats": ' . json_encode($stats) . '}';
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
            echo  '{"events": ' . json_encode($events) . '}';
        } catch(PDOException $e) {
            echo '{"error":{"text":'. $e->getMessage() .'}}';
        }     
    }
    //------------------Put Function-------------------------
    function updateArea($name, $area){
        $sql = "UPDATE arealist SET area=:name WHERE area=:area";
        try {
            $db = dbConnect();
            $stmt = $db->prepare($sql);
            $stmt->bindParam("name", $name);
            $stmt->bindParam("area", $area);
            $stmt->execute();
            $db = null;
        } catch(PDOException $e) {
            echo '{"error":{"text":'. $e->getMessage() .'}}';
        } 
    }
    
    function updateLocation($name,$address){
        $sql = "UPDATE devicelist SET location=:location WHERE address=:address";
        try {
            $db = dbConnect();
            $stmt = $db->prepare($sql);
            $stmt->bindParam("location", $name);
            $stmt->bindParam("address", $address);
            $stmt->execute();
            $db = null;
        } catch(PDOException $e) {
            echo '{"error":{"text":'. $e->getMessage() .'}}';
        } 
    }
    
    function updateZone($name,$id){
        $sql = "UPDATE devicestat SET alias=:alias WHERE id=:id";
        try {
            $db = dbConnect();
            $stmt = $db->prepare($sql);
            $stmt->bindParam("alias", $name);
            $stmt->bindParam("id", $id);
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
    
    function insertArea($area){
        $sql = "INSERT INTO arealist (area) value (:area)";
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