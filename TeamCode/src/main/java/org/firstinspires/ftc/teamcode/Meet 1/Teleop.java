package org.firstinspires.ftc.teamcode.DECODE;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

@TeleOp(name = "MainTeleOp", group = "Robot")
public class MainTeleOp extends LinearOpMode {

    // Drive motors
    private DcMotor rightBack, rightFront, leftFront, leftBack;

    // Scoring motors
    private DcMotor BottomMotor, TopMotor;

    // Turn sensitivity (lower = smoother/slower turning)
    private static final double TURN_SENSITIVITY = 0.8;

    // Strafe correction to prevent rotation during strafing
    private static final double STRAFE_CORRECTION = 0.15;

    // Variables for bumper control
    private boolean leftBumperPressed = false;
    private boolean rightBumperPressed = false;

    // Track if motors are moving to position
    private boolean bottomMotorMoving = false;
    private boolean topMotorMoving = false;

    @Override
    public void runOpMode() {
        // Hardware mapping
        rightBack = hardwareMap.get(DcMotor.class, "rightBack");
        rightFront = hardwareMap.get(DcMotor.class, "rightFront");
        leftFront = hardwareMap.get(DcMotor.class, "leftFront");
        leftBack = hardwareMap.get(DcMotor.class, "leftBack");

        BottomMotor = hardwareMap.get(DcMotor.class, "BottomMotor");
        TopMotor = hardwareMap.get(DcMotor.class, "TopMotor");

        // Set motor modes for encoder control
        BottomMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        TopMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        BottomMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        TopMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        // For parallel plate drivetrain - try reversing BOTH sides
        leftFront.setDirection(DcMotor.Direction.REVERSE);
        leftBack.setDirection(DcMotor.Direction.REVERSE);
        rightFront.setDirection(DcMotor.Direction.REVERSE);
        rightBack.setDirection(DcMotor.Direction.FORWARD);

        waitForStart();

        while (opModeIsActive()) {
            // ========== GAMEPAD 1: DRIVING CONTROLS ==========
            double y = -gamepad1.left_stick_y; // Forward/Back
            double x = gamepad1.left_stick_x * 1.1;  // Strafe Left/Right
            double turn = gamepad1.right_stick_x * TURN_SENSITIVITY; // Rotation with reduced sensitivity

            // Apply strafe correction to counteract unwanted rotation
            double strafeAdjustment = x * STRAFE_CORRECTION;

            // Mecanum formulas with strafe correction
            double frontLeftPower = y + x + turn - strafeAdjustment;
            double backLeftPower = y - x + turn + strafeAdjustment;
            double frontRightPower = y - x - turn + strafeAdjustment;
            double backRightPower = y + x - turn - strafeAdjustment;

            // Set power to drive motors
            leftFront.setPower(frontLeftPower);
            leftBack.setPower(backLeftPower);
            rightFront.setPower(frontRightPower);
            rightBack.setPower(backRightPower);

            // ========== GAMEPAD 2: SCORING CONTROLS ==========

            // --- Left Bumper: Move BottomMotor back 50 ticks ---
            if (gamepad2.left_bumper && !leftBumperPressed) {
                leftBumperPressed = true;
                int currentPos = BottomMotor.getCurrentPosition();
                int targetPos = currentPos - 50;
                BottomMotor.setTargetPosition(targetPos);
                BottomMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
                BottomMotor.setPower(1);
                bottomMotorMoving = true;
            } else if (!gamepad2.left_bumper) {
                leftBumperPressed = false;
            }

            // Check if BottomMotor reached target
            if (bottomMotorMoving && !BottomMotor.isBusy()) {
                BottomMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
                bottomMotorMoving = false;
            }

            // --- Right Bumper: Move TopMotor (outtake) back 100 ticks ---
            if (gamepad2.right_bumper && !rightBumperPressed) {
                rightBumperPressed = true;
                int currentPos = TopMotor.getCurrentPosition();
                int targetPos = currentPos - 100;
                TopMotor.setTargetPosition(targetPos);
                TopMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
                TopMotor.setPower(1);
                topMotorMoving = true;
            } else if (!gamepad2.right_bumper) {
                rightBumperPressed = false;
            }

            // Check if TopMotor reached target
            if (topMotorMoving && !TopMotor.isBusy()) {
                TopMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
                topMotorMoving = false;
            }

            // --- Scoring Motor Controls (Triggers) ---
            // Only control motors manually if they're not moving to position
            if (!bottomMotorMoving) {
                // Left trigger controls BottomMotor
                if (gamepad2.left_trigger > 0.1) {
                    BottomMotor.setPower(1);
                } else if (!gamepad2.b) {
                    BottomMotor.setPower(0);
                }
            }

            if (!topMotorMoving) {
                // Right trigger controls TopMotor
                if (gamepad2.right_trigger > 0.1) {
                    TopMotor.setPower(1);
                } else if (!gamepad2.b) {
                    TopMotor.setPower(0);
                }
            }

            // B button runs both motors forward
            if (gamepad2.b) {
                if (!bottomMotorMoving) {
                    BottomMotor.setPower(1);
                }
                if (!topMotorMoving) {
                    TopMotor.setPower(1);
                }
            }

            telemetry.addData("Motors", "Top: %.2f, Bottom: %.2f", TopMotor.getPower(), BottomMotor.getPower());
            telemetry.addData("Bottom Pos", BottomMotor.getCurrentPosition());
            telemetry.addData("Top Pos", TopMotor.getCurrentPosition());
            telemetry.addData("Bottom Moving", bottomMotorMoving);
            telemetry.addData("Top Moving", topMotorMoving);
            telemetry.addData("Turn Sensitivity", TURN_SENSITIVITY);
            telemetry.addData("Strafe Correction", STRAFE_CORRECTION);
            telemetry.update();
        }
    }
}
