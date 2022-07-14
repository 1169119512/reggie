package com.itheima.reggie.controller;


import com.itheima.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.reflect.FieldUtils;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 文件的上传和下载
 *由于图片文件读取是从jar包里读取，因此无法在jar包进行文件上传，而且无法使用getResource，需要使用相对路径，
 * 而文件下载必须使用getResourceAsStream，因为linux使用getResource不能访问jar包;
 *还有一种尝试就是使用向jar包添加文件的代码，但感觉并不好
 */
@RestController
@Slf4j
@RequestMapping("/common")
public class CommonController {
    @Value("${reggie.path}")
    private String basePath;


    /**
     *
     * 文件上传根据uuid改名并保存到本地
     * @param file
     * @return
     */
    @PostMapping("/upload")
    public R<String> upload(MultipartFile file){
        log.info(file.toString());
        String originalFileName = file.getOriginalFilename();
        String suffix = originalFileName.substring(originalFileName.lastIndexOf("."));
        String fileName = UUID.randomUUID().toString()+suffix;

//        //Linux上面部署为jar包,在Linux中无法直接访问未经解压的文件，所以就会找不到文件;
//        String basePath =this.getClass().getClassLoader().getResource("static/img/").getPath();//获取文件路径;
//        //无法用来上传文件到jar包 --假如参数是文件夹，就只能获取文件夹里的内容;是文件：获取文件的路径
//        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("static/img");
//        String basePath = new BufferedReader(new InputStreamReader(inputStream))
//                .lines().collect(Collectors.joining(System.lineSeparator()));
//        int lastIndex = fileName.lastIndexOf("/");
//        String basePath = fileName.substring(0,lastIndex);

        File dir = new File(basePath);
        if(!dir.exists()){
            dir.mkdirs();
        }


        try {
            file.transferTo(new File(basePath+fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return R.success(fileName);
    }


    /**
     * 文件下载并
     *
     *
     * @param name
     * @param response
     */
    @GetMapping("/download")
    public void download(String name, HttpServletResponse response){
        try {
            //Linux上面部署为jar包,在Linux中无法直接访问未经解压的文件，所以就会找不到文件
//            String fileName =this.getClass().getClassLoader().getResource("static/img/"+name).getPath();//获取文件路径;
//            FileInputStream fileInputStream = new FileInputStream(new File(fileName));
//            log.info(fileName);
            //用basePath+name方式并不好，因为在每个系统都需要自己设置好，但这是目前最好的选择
            FileInputStream fileInputStream = new FileInputStream(new File(basePath+name));
            //这个方式也有问题，无法读取到上传的图片,不过还是可以读取在本地的文件
//            InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("static/img/"+name);
            ServletOutputStream outputStream = response.getOutputStream();

            response.setContentType("image/jpeg");
            byte[] bytes = new byte[1024];
            int len = 0;
            while ((len = (fileInputStream.read(bytes)))!= -1){
                outputStream.write(bytes);
                outputStream.flush();
            }
            fileInputStream.close();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }




    }



}
