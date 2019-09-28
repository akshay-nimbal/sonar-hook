/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sonar;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;

/**
 *
 * @author akshay.n
 */
public class SonarSVNHook {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException, InterruptedException {

        String configFilePath = System.getenv("SONAR_HOOK_HOME") + "/sonar-project.properties";
        Properties prop = new Properties();
        prop.load(new FileInputStream(configFilePath));

        String sonar_host = prop.getProperty("sonar.host.url");
        File file = new File(System.getenv("SONAR_HOOK_HOME") + "\\logs\\log.txt");
        String[] cmd_user = {"cmd.exe", "/c", "whoami"};

        Process p_user = Runtime.getRuntime().exec(cmd_user);

        BufferedReader input_user = new BufferedReader(new InputStreamReader(p_user.getInputStream()));
        String projectKey = input_user.readLine();
        FileWriter fileWriter = new FileWriter(file);
        fileWriter.write("\n####### Directory is =" +prop);
        String directory = null;
        if (prop.containsKey("sonar.scan.directory")) {
            directory = prop.getProperty("sonar.scan.directory");
        } else {
            if (args.length != 0) {
                directory = args[2];
            }
        }
        if (prop.containsKey("sonar.projectKey")) {
            projectKey = prop.getProperty("sonar.projectKey");
        }
        fileWriter.write("###### User/ProjectKey is=" + projectKey);
        fileWriter.write("\n####### Directory is =" + directory);
        String[] cmd = {"cmd.exe", "/c", "svn diff --summarize" + " " + directory};

        Process p = Runtime.getRuntime().exec(cmd);

        BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));

        String temp = "";
        String inclusions = "";

        while ((temp = input.readLine()) != null) {
            if (temp.indexOf("java") < 0 && temp.indexOf("js") < 0 && temp.indexOf("jsp") < 0 && temp.indexOf("html") < 0
                    && temp.indexOf("css") < 0 && temp.indexOf("ts") < 0) {
                continue;
            }
            inclusions = inclusions.concat(processFileName(temp, directory) + ",");
        }

        fileWriter.write("\n####### Inclusions/Modified files =" + inclusions);
        if (inclusions.isEmpty()) {
            System.exit(0);
        }
        String sonar_cmd = String.format("sonar-runner -X -Dsonar.verbose=true -Dsonar.host.url=%s"
                + " -Dsonar.projectKey=%s -Dsonar.scm.disabled=true -Dsonar.inclusions=%s  -Dsonar.java.binaries=%s", sonar_host, projectKey, inclusions,
                System.getenv("SONAR_HOOK_HOME") + "\\temp");

        String[] cmd2 = {"cmd.exe", "/c", sonar_cmd};
        Process p2 = Runtime.getRuntime().exec(cmd2);
        BufferedReader input2 = new BufferedReader(new InputStreamReader(p2.getInputStream()));

        while ((temp = input2.readLine()) != null) {
            fileWriter.write(temp);
            fileWriter.write("\n");
        }

        String status = "";
        while (true) {
            URL url = new URL(sonar_host + "/api/ce/component?component=" + projectKey);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            BufferedReader input5 = new BufferedReader(new InputStreamReader(con.getInputStream()));
            status = input5.readLine();
            fileWriter.write("\n####### Submitted the files to server, report is generating..........." + status);
            if (status != null && status.charAt(10) == ']') {
                if (input5 != null) {
                    input5.close();
                }
                break;
            }
            Thread.sleep(1000);
        }
        fileWriter.write("\n###### Analysis completed!!..Checking the quality gate report!!");
        URL url2 = new URL(sonar_host + "/api/qualitygates/project_status?projectKey=" + projectKey);
        HttpURLConnection con2 = (HttpURLConnection) url2.openConnection();
        con2.setRequestMethod("GET");
        BufferedReader input3 = new BufferedReader(new InputStreamReader(con2.getInputStream()));
        String qg_output = "";
        qg_output = input3.readLine();
        if (input != null && input2 != null) {
            input.close();
            input2.close();
        }

        if (qg_output != null && !qg_output.isEmpty()) {
            if (qg_output.charAt(28) == 'O') {
                fileWriter.write("\n########## Quality gate success" + qg_output);
                if (input3 != null) {
                    input3.close();
                }
                fileWriter.flush();
                fileWriter.close();
                System.exit(0);
            } else if (qg_output.charAt(28) == 'E') {
                fileWriter.write("\n########## Quality gate failure" + qg_output);
                if (input3 != null) {
                    input3.close();
                }
                fileWriter.flush();
                fileWriter.close();
                System.exit(1);
            } else {
                fileWriter.write("\n########## Quality gate is still in process..please check the api!" + qg_output);
                if (input3 != null) {
                    input3.close();
                }
                fileWriter.flush();
                fileWriter.close();
                System.exit(1);
            }
        } else {
            fileWriter.write("\n######### Quality gate output is null! PLease contact admin :)" + qg_output);
            System.exit(1);
            fileWriter.close();
        }
    }

    public static String processFileName(String name, String dir) throws IOException {
        name = name.replaceAll("\\s", "");
        name = name.replace("M" + dir, "");
        return name;
    }
}