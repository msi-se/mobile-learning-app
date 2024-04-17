let express = require("express");
let app = express();
let dotenv = require("dotenv");
let exec = require('child_process').exec;

// load the environment variables from the .env file
dotenv.config();

app.get("/update-server", function(req, res) {

    // get the authoization header from the request
    let auth = req.get("Authorization");

    // check if the authorization header is valid
    if (auth === process.env.ADMIN_TOKEN) {
        
        // update the server
        exec("cd /home/loco/team-learning && docker-compose down && git pull && docker-compose up -d --build", (err, stdout, stderr) => {
            if (err) {
                console.error(err);
                return;
            }
            console.log(stdout);
        });
        
        res.status(200).send("Server updated successfully");
    } else {
        // return unauthorized status code
        res.status(401).send("Unauthorized");
    }
});

app.listen(3000, function() {
    console.log("Server is running on port 3000");
});