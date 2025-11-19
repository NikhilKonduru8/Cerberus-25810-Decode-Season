package org.firstinspires.ftc.teamcode;

import com.bylazar.configurables.annotations.Configurable;
import com.bylazar.telemetry.PanelsTelemetry;
import com.bylazar.telemetry.TelemetryManager;
import com.pedropathing.follower.Follower;
import com.pedropathing.geometry.BezierLine;
import com.pedropathing.geometry.Pose;
import com.pedropathing.paths.PathChain;
import com.pedropathing.util.Timer;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import org.firstinspires.ftc.teamcode.pedroPathing.Constants;

@Autonomous(name = "TestAutoPedro", group = "Autonomous")
@Configurable // Panels
public class TestAutoPedro extends OpMode {

    private TelemetryManager panelsTelemetry; // Panels Telemetry instance
    public Follower follower; // Pedro Pathing follower instance
    private int pathState; // Current autonomous path state (state machine)
    private Paths paths; // Paths defined in the Paths class
    private Timer pathTimer; // Timer for path state changes

    @Override
    public void init() {
        panelsTelemetry = PanelsTelemetry.INSTANCE.getTelemetry();
        pathTimer = new Timer();

        follower = Constants.createFollower(hardwareMap);
        follower.setStartingPose(new Pose(26.065, 125.199, Math.toRadians(90)));

        paths = new Paths(follower); // Build paths
        pathState = 0; // Initialize state machine to first state

        panelsTelemetry.debug("Status", "Initialized");
        panelsTelemetry.update(telemetry);
    }

    @Override
    public void start() {
        pathTimer.resetTimer();
        setPathState(0);
    }

    @Override
    public void loop() {
        follower.update(); // Update Pedro Pathing
        autonomousPathUpdate(); // Update autonomous state machine

        // Log values to Panels and Driver Station
        panelsTelemetry.debug("Path State", pathState);
        panelsTelemetry.debug("X", follower.getPose().getX());
        panelsTelemetry.debug("Y", follower.getPose().getY());
        panelsTelemetry.debug("Heading", Math.toDegrees(follower.getPose().getHeading()));
        panelsTelemetry.debug("Is Busy", follower.isBusy());
        panelsTelemetry.update(telemetry);
    }

    public static class Paths {

        public PathChain FirstShoot;

        public Paths(Follower follower) {
            FirstShoot = follower
                    .pathBuilder()
                    .addPath(
                            new BezierLine(new Pose(26.065, 125.199), new Pose(48.071, 95.288))
                    )
                    .setLinearHeadingInterpolation(Math.toRadians(90), Math.toRadians(136))
                    .build();

            // Add more paths here as needed:
            // Example:
            // SecondPath = follower
            //         .pathBuilder()
            //         .addPath(new BezierLine(new Pose(...), new Pose(...)))
            //         .build();
        }
    }

    public void autonomousPathUpdate() {
        switch (pathState) {
            case 0:
                // Start the first path
                follower.followPath(paths.FirstShoot);
                setPathState(1);
                break;

            case 1:
                // Wait for path to complete, then stop
                if(!follower.isBusy()) {
                    setPathState(-1);
                }
                break;
        }
    }

    /**
     * Changes the state of the path state machine and resets the timer
     * @param pState The new path state
     */
    public void setPathState(int pState) {
        pathState = pState;
        pathTimer.resetTimer();
    }
}
