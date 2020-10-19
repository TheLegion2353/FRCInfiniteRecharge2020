/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot;

import edu.wpi.first.wpilibj.I2C;
import edu.wpi.first.wpilibj.util.Color;

import com.revrobotics.ColorSensorV3;
import com.revrobotics.ColorMatchResult;
import com.revrobotics.ColorMatch;

import edu.wpi.first.cameraserver.CameraServer;
import edu.wpi.first.wpilibj.AnalogInput;
import edu.wpi.first.wpilibj.AnalogPotentiometer;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.PWMVictorSPX;
import edu.wpi.first.wpilibj.SpeedControllerGroup;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.Ultrasonic;
import edu.wpi.first.wpilibj.VictorSP;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.GenericHID.Hand;
import edu.wpi.first.wpilibj.controller.PIDController;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the TimedRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the build.gradle file in the
 * project.
 */
public class Robot extends TimedRobot {
  boolean autoDone = false;
  private static final String kDefaultAuto = "Default";
  private static final String kCustomAuto = "My Auto";
  private String m_autoSelected;
  private final SendableChooser<String> m_chooser = new SendableChooser<>();
  private final Timer m_timer = new Timer();
  private final I2C.Port i2cPort = I2C.Port.kOnboard;

  private final ColorSensorV3 m_colorSensor = new ColorSensorV3(i2cPort);

  private final ColorMatch m_colorMatcher = new ColorMatch();

  private final Color redSide = ColorMatch.makeColor(1.0, 0.0, 0.0);
  private final Color blueSide = ColorMatch.makeColor(0.25, 0.25, 1.0);
  private final Color floorColor = ColorMatch.makeColor(0.0, 0.0, 0.0);
  private final Color whiteColor = ColorMatch.makeColor(1.0, 1.0, 1.0);
  private final Color brightRed = ColorMatch.makeColor(1.0, .5, .5);
  private final Color floor2Color = ColorMatch.makeColor(.27, .27, .27);

  public Joystick logitech = new Joystick(0);

  // Robot stuff and motors
  // drive motors
  public PWMVictorSPX m_leftDrive1 = new PWMVictorSPX(6); // setting up VictorSPX on port 6
  public PWMVictorSPX m_leftDrive2 = new PWMVictorSPX(7);
  public PWMVictorSPX m_rightDrive1 = new PWMVictorSPX(8); // This motor controller is a VictoSP, not VictorSPX
  public PWMVictorSPX m_rightDrive2 = new PWMVictorSPX(4);

  // intake motor
  public PWMVictorSPX m_intake = new PWMVictorSPX(0); //intake motor on port 4

  // lift motors
  public PWMVictorSPX m_scissor = new PWMVictorSPX(1); // left intake on port 0

  public VictorSP m_snow = new VictorSP(2);

  // setting up differential drive which is basically tank drive
  public SpeedControllerGroup m_left = new SpeedControllerGroup(m_leftDrive1, m_leftDrive2);
  public SpeedControllerGroup m_right = new SpeedControllerGroup(m_rightDrive1, m_rightDrive2);

  public DifferentialDrive m_drive = new DifferentialDrive(m_left, m_right);

  /**
   * This function is run when the robot is first started up and should be used
   * for any initialization code.
   */

  @Override
  public void robotInit() {
    CameraServer.getInstance().startAutomaticCapture();
    m_chooser.setDefaultOption("Default Auto", kDefaultAuto);
    m_chooser.addOption("My Auto", kCustomAuto);
    SmartDashboard.putData("Auto choices", m_chooser);
    m_colorMatcher.addColorMatch(redSide);
    m_colorMatcher.addColorMatch(blueSide);
    m_colorMatcher.addColorMatch(floor2Color);
    m_colorMatcher.addColorMatch(floorColor);
    m_colorMatcher.addColorMatch(whiteColor);
    autoDone = false;
    m_snow.setSpeed(0);
    m_scissor.setSpeed(0);
    m_intake.setSpeed(0);
  }

  /**
   * This function is called every robot packet, no matter the mode. Use this for
   * items like diagnostics that you want ran during disabled, autonomous,
   * teleoperated and test.
   *
   * <p>
   * This runs after the mode specific periodic functions, but before LiveWindow
   * and SmartDashboard integrated updating.
   */
  @Override
  public void robotPeriodic() {
  }

  /**
   * This autonomous (along with the chooser code above) shows how to select
   * between different autonomous modes using the dashboard. The sendable chooser
   * code works with the Java SmartDashboard. If you prefer the LabVIEW Dashboard,
   * remove all of the chooser code and uncomment the getString line to get the
   * auto name from the text box below the Gyro
   *
   * <p>
   * You can add additional auto modes by adding additional comparisons to the
   * switch structure below with additional strings. If using the SendableChooser
   * make sure to add them to the chooser code above as well.
   */
  @Override
  public void autonomousInit() {
    m_autoSelected = m_chooser.getSelected();
    // m_autoSelected = SmartDashboard.getString("Auto Selector", kDefaultAuto);
    System.out.println("Auto selected: " + m_autoSelected);
    autoDone = false;
    m_timer.reset();
    m_timer.start();
  }

  /**
   * This function is called periodically during autonomous.
   */
  @Override
  public void autonomousPeriodic() {
    switch (m_autoSelected) {
    case kCustomAuto:
      // Put custom auto code here
      break;
    case kDefaultAuto:
    default:
      // Put default auto code here
      if (!autoDone) {
        autoDone = true;
        /*
        autoDone = true;
        m_timer.reset();
        m_timer.start();
        while (m_timer.get() < 5) {
          m_snow.setSpeed(-1);
        }
        m_snow.setSpeed(0);
        m_timer.reset();
        m_timer.start();
        while (m_timer.get() < 2) {          
          m_drive.tankDrive(-.4, -.4);
        }
        */

        
        Color detectedColor = m_colorSensor.getColor();
        ColorMatchResult match = m_colorMatcher.matchClosestColor(detectedColor);

        while (!match.color.equals(redSide) && !match.color.equals(blueSide) && !match.color.equals(brightRed) && m_timer.get() <= 6.5){//6.5
          detectedColor = m_colorSensor.getColor();
          match = m_colorMatcher.matchClosestColor(detectedColor);
          m_drive.tankDrive(-.40, -.40);
        }
        m_timer.reset();
        m_timer.start();
        
        while (m_timer.get() < 5) {          
          m_drive.tankDrive(-.4, -.4);
        }
        
        m_timer.reset();
        m_timer.start();
        while (m_timer.get() < 3.5) {
          m_scissor.setSpeed(-0.475);
          m_drive.tankDrive(-.4, -.4);
        }
        m_drive.tankDrive(0.0, 0.0);
        m_scissor.setSpeed(0);

      /*
      m_timer.reset();
      m_timer.start();
      while (m_timer.get() <= 5) {

      }

      m_timer.reset();
      m_timer.start();
      while (m_timer.get() <= 3.5) {
        m_scissor.setSpeed(0.5);
      }

      */
      /*

      m_timer.reset();
      m_timer.start();
      while (m_timer.get() <= 4) {
        m_drive.tankDrive(0.5, 0.5);
      }
      m_drive.tankDrive(0, 0);
      */


      

    }
      m_drive.tankDrive(0.0, 0.0);

      
      

      break;  
  }
  }

  /**
   * This function is called periodically during operator control.
   */

  public double x(final double conVal) {
    double outVal = 0;
    if (Math.abs(conVal) < .1f) {
      outVal = 0;
    } else if (conVal >= 0) {
      outVal = Math.pow(10, conVal - 1.10914) + 1 - Math.pow(10, -0.10914);
    } else {
      outVal = -(Math.pow(10, ((-conVal) - 1.10914)) + 1 - Math.pow(10, -0.10914));
    }

    return outVal;
  }

  @Override
  public void teleopPeriodic() {

    m_drive.tankDrive(x(-logitech.getY(Hand.kLeft)), x(-logitech.getRawAxis(5))); //hand specifies the left or right side of the controller
    m_intake.setSpeed((logitech.getRawAxis(2) - logitech.getRawAxis(3)) / 1);
    if (logitech.getRawButton(1)) {
      m_scissor.setSpeed(.5);
    } else if (logitech.getRawButton(2)) {
      m_scissor.setSpeed(-.5);
    } else {
      m_scissor.setSpeed(0);
    }

    if (logitech.getRawButton(4)) {
      m_drive.tankDrive(-.4, -.4);
    } else if (logitech.getRawButton(3)) {
      
    } else {
      m_snow.setSpeed(0);
    }
  }

  /**
   * This function is called periodically during test mode.
   */
  @Override
  public void testPeriodic() {
  }
}
