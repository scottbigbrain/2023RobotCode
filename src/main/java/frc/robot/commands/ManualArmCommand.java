package frc.robot.commands;

import edu.wpi.first.wpilibj2.command.CommandBase;
import edu.wpi.first.wpilibj2.command.button.CommandJoystick;
import frc.robot.subsystems.ArmSubsystem;

public class ManualArmCommand extends CommandBase {

    private final ArmSubsystem m_armSubsystem;
    private final CommandJoystick m_controller;

    public ManualArmCommand(ArmSubsystem subsystem, CommandJoystick xboxController){
        m_armSubsystem = subsystem;
        m_controller = xboxController;

        addRequirements(subsystem);

        configureButtonBindings();
    }

    private void configureButtonBindings(){
        m_controller.button(0).whileTrue(m_armSubsystem.m_pulleySubsystem.extendCommand());
        m_controller.button(1).whileTrue(m_armSubsystem.m_pulleySubsystem.retractCommand());

        m_controller.button(2).whileTrue(m_armSubsystem.m_manipulatorSubsystem.rawUpCommand());
        m_controller.button(3).whileTrue(m_armSubsystem.m_manipulatorSubsystem.rawDownCommand());

        m_controller.button(4).onTrue(m_armSubsystem.m_manipulatorSubsystem.intakeConeCommand());
        m_controller.button(5).onTrue(m_armSubsystem.m_manipulatorSubsystem.intakeCubeCommand());
        m_controller.button(6).onTrue(m_armSubsystem.m_manipulatorSubsystem.smartDropCommand());
    }
    @Override
    public void execute() {

        double yAxis = m_controller.getY();

        m_armSubsystem.setElevatorPower(-yAxis);
    }

    @Override
    public boolean isFinished() {
        return false;
    }
    
}
