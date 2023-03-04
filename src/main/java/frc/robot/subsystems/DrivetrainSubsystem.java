package frc.robot.subsystems;

import edu.wpi.first.wpilibj.ADXRS450_Gyro;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import edu.wpi.first.wpilibj2.command.Command.InterruptionBehavior;
import edu.wpi.first.wpilibj.SPI;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.robot.Constants;
import frc.robot.util.Vector;
import frc.robot.wrappers.SwerveModule;
// import com.kauailabs.navx.frc.AHRS;

public class DrivetrainSubsystem extends SubsystemBase {
    private final SwerveModule frontRight;
    private final SwerveModule frontLeft;
    private final SwerveModule backRight;
    private final SwerveModule backLeft;

    private final double frontRightDefault = Constants.DRIVE_THETA_OFFSET;
    private final double frontLeftDefualt = -Constants.DRIVE_THETA_OFFSET;
    private final double backRightDefault = -Constants.DRIVE_THETA_OFFSET;
    private final double backLeftDefault = Constants.DRIVE_THETA_OFFSET;

    public SwerveModuleState frontRightState = new SwerveModuleState(0, Rotation2d.fromDegrees(frontRightDefault));
    public SwerveModuleState frontLeftState = new SwerveModuleState(0, Rotation2d.fromDegrees(frontLeftDefualt));
    public SwerveModuleState backRightState = new SwerveModuleState(0, Rotation2d.fromDegrees(backRightDefault));
    public SwerveModuleState backLeftState = new SwerveModuleState(0, Rotation2d.fromDegrees(backLeftDefault));

    public ADXRS450_Gyro m_gyro = new ADXRS450_Gyro(SPI.Port.kOnboardCS0);
    public double gyroOffset = 0;
    // public AHRS m_gyro = new AHRS(SPI.Port.kMXP);

    public boolean field_centric_drive = true;

    /* Odometry */
    public Vector frontLeftVelocity = new Vector();
    public Vector frontRightVelocity = new Vector();
    public Vector backRightVelocity = new Vector();
    public Vector backLeftVelocity = new Vector();
    
    private Vector deltaTranslation = new Vector();
    private Vector translation = new Vector();
    private double heading = 0.;
    private double headingOffset = 0.;
    
    public DrivetrainSubsystem() {
        this.frontRight = new SwerveModule(Constants.FRONT_RIGHT_MODULE_DRIVE_MOTOR, Constants.FRONT_RIGHT_MODULE_STEER_MOTOR, 
                                        Constants.FRONT_RIGHT_MODULE_STEER_ENCODER, Constants.FRONT_RIGHT_MODULE_STEER_OFFSET,
                                        Constants.FRONT_RIGHT_REVERSE_DRIVE);
        this.frontLeft = new SwerveModule(Constants.FRONT_LEFT_MODULE_DRIVE_MOTOR, Constants.FRONT_LEFT_MODULE_STEER_MOTOR, 
                                        Constants.FRONT_LEFT_MODULE_STEER_ENCODER, Constants.FRONT_LEFT_MODULE_STEER_OFFSET,
                                        Constants.FRONT_LEFT_REVERSE_DRIVE);
        this.backRight = new SwerveModule(Constants.BACK_RIGHT_MODULE_DRIVE_MOTOR, Constants.BACK_RIGHT_MODULE_STEER_MOTOR, 
                                        Constants.BACK_RIGHT_MODULE_STEER_ENCODER, Constants.BACK_RIGHT_MODULE_STEER_OFFSET,
                                        Constants.BACK_RIGHT_REVERSE_DRIVE);
        this.backLeft = new SwerveModule(Constants.BACK_LEFT_MODULE_DRIVE_MOTOR, Constants.BACK_LEFT_MODULE_STEER_MOTOR, 
                                        Constants.BACK_LEFT_MODULE_STEER_ENCODER, Constants.BACK_LEFT_MODULE_STEER_OFFSET,
                                        Constants.BACK_LEFT_REVERSE_DRIVE);
    
        // frontRight is a problem child and wants to have its own PID values :(
        // this.frontRight.setRotationPID(0.002, 0.000, 0.00001);
        
    }

    public void setFrontRight(SwerveModuleState state) {
        frontRightState = state;
    }
    public void setFrontLeft(SwerveModuleState state) {
        frontLeftState = state;
    }
    public void setBackRight(SwerveModuleState state) {
        backRightState = state;
    }
    public void setBackLeft(SwerveModuleState state) {
        backLeftState = state;
    }

    public void setAllToState(SwerveModuleState state) {
        setFrontRight(state);
        setFrontLeft(state);
        setBackRight(state);
        setBackLeft(state);
    }

    public void resetGyroscope() {
        gyroOffset = 0;
        m_gyro.reset();
    }

    public void setGyroOffset(double x) {
        gyroOffset = x;
    }

    // public void gyroFromMag(double zeroHeading) {
    //     m_gyro.setAngleAdjustment(m_gyro.getCompassHeading() - zeroHeading - m_gyro.getYaw());
    // }

    public double getGyro() {
        return m_gyro.getAngle() + gyroOffset;
    } 

    public void periodic() {
        frontRight.setState(frontRightState);
        frontLeft.setState(frontLeftState);
        backRight.setState(backRightState);
        backLeft.setState(backLeftState);

        // System.out.println("FR: " + frontRight.getRotation() + "\tFL: " + frontLeft.getRotation() +
        //                     "\tBR: " + backRight.getRotation() + "\tBL: " + backLeft.getRotation());

        // System.out.println("Absolute: " + frontLeft.getRotationAbsolute()+"\t NEO"+ frontLeft.getNeoRotation()+"\t Calab"+ frontLeft.getRotation());
    }

    public void driveVector(double speed, double direction, double aSpeed) {
        double driveSpeed = speed * Constants.MAX_SPEED;
        double driveAngle = direction - getGyro();  // field-centric

        SwerveModuleState frontLeftDrive = new SwerveModuleState(driveSpeed, Rotation2d.fromDegrees(driveAngle));
        SwerveModuleState frontRightDrive = new SwerveModuleState(driveSpeed, Rotation2d.fromDegrees(driveAngle));
        SwerveModuleState backLeftDrive = new SwerveModuleState(driveSpeed, Rotation2d.fromDegrees(driveAngle));
        SwerveModuleState backRightDrive = new SwerveModuleState(driveSpeed, Rotation2d.fromDegrees(driveAngle));
        
        double rotateSpeed = Constants.MAX_SPEED * aSpeed;

        // different signs accounts for orientation of modules
        SwerveModuleState frontLeftRotate =  new SwerveModuleState(-rotateSpeed, Rotation2d.fromDegrees( Constants.DRIVE_THETA_OFFSET));
        SwerveModuleState frontRightRotate = new SwerveModuleState( rotateSpeed, Rotation2d.fromDegrees(-Constants.DRIVE_THETA_OFFSET));
        SwerveModuleState backLeftRotate =   new SwerveModuleState(-rotateSpeed, Rotation2d.fromDegrees(-Constants.DRIVE_THETA_OFFSET));
        SwerveModuleState backRightRotate =  new SwerveModuleState( rotateSpeed, Rotation2d.fromDegrees( Constants.DRIVE_THETA_OFFSET));

        setFrontLeft(this.addStates(frontLeftDrive, frontLeftRotate));
        setFrontRight(this.addStates(frontRightDrive, frontRightRotate));
        setBackLeft(this.addStates(backLeftDrive, backLeftRotate));
        setBackRight(this.addStates(backRightDrive, backRightRotate));
    }
    public Command driveVectorCommand(double speed, double angle, double aSpeed) {
        return Commands.startEnd(
            () -> {this.driveVector(speed, angle, aSpeed);},
            this::lockDrive,
            this
            ).withInterruptBehavior(InterruptionBehavior.kCancelSelf);
    }

    public void driveXY(double xSpeed, double ySpeed, double aSpeed) {
        double speed = Math.sqrt(xSpeed*xSpeed + ySpeed*ySpeed);
        double direction = Math.atan(ySpeed/xSpeed);
        driveVector(speed, direction, aSpeed);
    }

    public void driveArcade(double xSpeed, double ySpeed, double aSpeed){
        double driveAngle = 0;
        if (!(xSpeed == 0 && ySpeed == 0)) {
            // driveAngle = Math.toDegrees(Math.atan(leftX / leftY));
            driveAngle = Math.toDegrees(Math.atan2(-xSpeed, ySpeed));
            // adjusts negative angles to be in range of 0 to 360
            if (driveAngle < 0) {
                driveAngle += 360;
            }
        }

        if (!field_centric_drive) {
            driveAngle += getGyro();
        }

        double speed = Math.abs(Math.hypot(xSpeed, ySpeed));

        // if (speed > 0) {
            driveVector(speed, driveAngle, aSpeed);
        // } else {
        //     allToRestState();
        // }
    }

    // public void allToRestState() {
    //     SwerveModuleState frontLeftRotate =  new SwerveModuleState(0, Rotation2d.fromDegrees( Constants.DRIVE_THETA_OFFSET));
    //     SwerveModuleState frontRightRotate = new SwerveModuleState(0, Rotation2d.fromDegrees(-Constants.DRIVE_THETA_OFFSET));
    //     SwerveModuleState backLeftRotate =   new SwerveModuleState(0, Rotation2d.fromDegrees(-Constants.DRIVE_THETA_OFFSET));
    //     SwerveModuleState backRightRotate =  new SwerveModuleState(0, Rotation2d.fromDegrees( Constants.DRIVE_THETA_OFFSET));
    // }
    public void lockDrive(){
        setBackLeft(new SwerveModuleState(0.0, Rotation2d.fromDegrees(-45.0)));
        setBackRight(new SwerveModuleState(0.0, Rotation2d.fromDegrees(45.0)));
        setFrontLeft(new SwerveModuleState(0.0, Rotation2d.fromDegrees(-45.0)));
        setFrontRight(new SwerveModuleState(0.0, Rotation2d.fromDegrees(45.0)));
    }

    public boolean toggleFieldCentricDrive() {
        field_centric_drive = !field_centric_drive;
        return field_centric_drive;
    }

    private SwerveModuleState addStates(SwerveModuleState a, SwerveModuleState b) {
        // here we treat ServeModuleState's as vectors and add their component forms 
        // the cart is short for cartesian bc why not
        // [0] is x and [1] is y
        double[] a_cart = {a.speedMetersPerSecond*a.angle.getCos(), a.speedMetersPerSecond*a.angle.getSin()};
        double[] b_cart = {b.speedMetersPerSecond*b.angle.getCos(), b.speedMetersPerSecond*b.angle.getSin()};
        double[] out_cart = {a_cart[0] + b_cart[0], a_cart[1] + b_cart[1]};
        
        double speed = Math.hypot(out_cart[0], out_cart[1]);
        if (out_cart[0] < 0) speed *= -1; // this is needed bc Math.atan() only returns -90 to 90, so for negative x it needs to flip
        double angle = Math.toDegrees(Math.atan(out_cart[1] / out_cart[0]));
        SwerveModuleState out = new SwerveModuleState(speed, Rotation2d.fromDegrees(angle));

        return out;
    }

    public void resetPose(Pose2d pose){
        resetPose(pose.getX(), pose.getY(), pose.getRotation().getRadians());
    }
    public void resetPose(double x, double y, double a){
        this.translation.x = x;
        this.translation.y = y;
        this.headingOffset = a-this.getGyro();
        this.heading = a;
    }
    public void resetPose(){
        resetPose(0, 0, 0);
    }

    public Pose2d getPose(){
        return new Pose2d(translation.x, translation.y, new Rotation2d(Math.toRadians(heading)));
    }

    public void updateOdometry(){
        frontLeft.updateOdometry();
        frontRight.updateOdometry();
        backRight.updateOdometry();
        backLeft.updateOdometry();

        frontLeftVelocity = frontLeft.getVelocity();
        frontRightVelocity = frontLeft.getVelocity();
        backLeftVelocity = backLeft.getVelocity();
        backRightVelocity = backLeft.getVelocity();

        // System.out.println("FR_V: "+frontLeftVelocity.toString());
        
        heading = this.getGyro()+headingOffset;
        SmartDashboard.putNumber("Gyro", getGyro());
        // System.out.println(this.getGyro());

        deltaTranslation = calculateDeltaPosition();
        translation = translation.add(deltaTranslation);

    }

    private Vector calculateDeltaPosition(){
        // SmartDashboard.putNumber("A1", frontLeft.getRotation());
        // SmartDashboard.putNumber("A2", frontRight.getRotation());
        // SmartDashboard.putNumber("A3", backLeft.getRotation());
        // SmartDashboard.putNumber("A4", backLeft.getRotation());
        // SmartDashboard.putNumber("V1", frontLeft.getSpeedMetersPerSecond());
        // SmartDashboard.putNumber("V2", frontRight.getSpeedMetersPerSecond());
        // SmartDashboard.putNumber("V3", backLeft.getSpeedMetersPerSecond());
        // SmartDashboard.putNumber("V4", backRight.getSpeedMetersPerSecond());

        // SmartDashboard.putNumber("ratio", backLeft.getSpeedMetersPerSecond() / frontLeft.getSpeedMetersPerSecond());

        Vector vectorSum =  frontLeftVelocity.add(
                            frontRightVelocity.add(
                            backLeftVelocity.add(
                            backRightVelocity
                            ))).scale(0.25);

        Vector rotatedVector = vectorSum.copy();
        rotatedVector.rotateByAngle(Math.toRadians(-heading));

        return rotatedVector;
    }

    public void mergeCameraPose(Pose2d cameraPose, double confidence){
        Vector newTranslation = translation.scale( 1-confidence ).add(
                      Vector.fromTranslation(cameraPose.getTranslation().times( confidence )));
        
        double headingConfidence = confidence * 0.01;
        double newHeading  = ( heading * (1.0-headingConfidence) ) + ( cameraPose.getRotation().getDegrees() * headingConfidence );

        resetPose(newTranslation.x, newTranslation.y, newHeading);
    }

    public void disable(){
        frontLeft.disable();
        frontRight.disable();
        backLeft.disable();
        backRight.disable();
    }
    public void enable(){
        frontLeft.enable();
        frontRight.enable();
        backLeft.enable();
        backRight.enable();
    }

}
