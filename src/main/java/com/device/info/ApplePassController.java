package com.device.info;


import com.google.api.client.util.DateTime;
import de.brendamour.jpasskit.*;
import de.brendamour.jpasskit.apns.PKSendPushNotificationUtil;
import de.brendamour.jpasskit.enums.PKBarcodeFormat;
import de.brendamour.jpasskit.enums.PKPassType;
import de.brendamour.jpasskit.passes.PKGenericPass;
import de.brendamour.jpasskit.signing.*;
import de.brendamour.jpasskit.util.CertUtils;
import com.google.gson.JsonObject;
import org.apache.commons.io.IOUtils;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

//import com.ryantenney.passkit4j.Pass;
//import com.ryantenney.passkit4j.PassResource;
//import com.ryantenney.passkit4j.PassSerializer;
//import com.ryantenney.passkit4j.io.NamedInputStreamSupplier;
//import com.ryantenney.passkit4j.model.Barcode;
//import com.ryantenney.passkit4j.model.BarcodeFormat;
//import com.ryantenney.passkit4j.model.Color;
//import com.ryantenney.passkit4j.model.Field;
//import com.ryantenney.passkit4j.model.Generic;
//import com.ryantenney.passkit4j.model.PassInformation;
//import com.ryantenney.passkit4j.model.TextField;
//import com.ryantenney.passkit4j.sign.PassSigner;
//import com.ryantenney.passkit4j.sign.PassSignerImpl;
//import com.ryantenney.passkit4j.sign.PassSigningException;

import javax.servlet.http.HttpServletRequest;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
//import java.util.Base64;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/pass/apple/")
public class ApplePassController {

    private String authToken;
    private String pushToken;

//    @GetMapping(path = "/create", produces = "application/vnd.apple.pkpass")
//    public ResponseEntity<byte[]> createAppleFlashPass() throws GeneralSecurityException, IOException {
//        System.out.println("Creating pass");
//        Pass pass = new Pass()
//                .description("MFPT")
//                .passTypeIdentifier("pass.com.entrustdatacard.idissuance")
//                .organizationName("EDC")
//                .logoText("EDC")
//                .foregroundColor(new Color(50, 50, 50))
//                .labelColor(new Color(100, 100, 100))
//                .backgroundColor(new Color(150, 150, 150))
//                .webServiceURL("https://apple-pass-poc.herokuapp.com/pass/apple/register")
//                .authenticationToken("1111111111111111")
//                .serialNumber("1");
//
//        pass.barcode(new Barcode(BarcodeFormat.QR, "BarcodeValue")
//                .altText("BarcodeValue")
//        );
//
//        byte[] logo2x = IOUtils.toByteArray(getClass().getResourceAsStream("/SampleJpg.jpg"));
//        //byte[] logo = downsample(logo2x, 0.5f);
//
//        List<NamedInputStreamSupplier> files = new ArrayList<>();
//        files.add(new PassResource("icon.png", logo2x));
//        files.add(new PassResource("icon@2x.png", logo2x));
//        files.add(new PassResource("logo.png", logo2x));
//        files.add(new PassResource("logo@2x.png", logo2x));
//
//        files.add(new PassResource("thumbnail@2x.png", logo2x));
//        files.add(new PassResource("thumbnail.png", logo2x));
//
//        pass.files(files);
//
//        PassInformation info = new Generic()
//                .primaryFields(
//                        new TextField("userFullName",
//                                "Full Name",
//                                "Arpit")
//                ).headerFields(
//                        new TextField("passTitle", "MFPT")
//                );
//
//        info.secondaryFields(
//                new TextField("userRole",
//                        "Role",
//                        "Admin")
//        );
//
//        List<Field<?>> auxFields = new ArrayList<>();
//        auxFields.add(new TextField("userNumber",
//                "userNumber",
//                "1"));
//
//        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
//        auxFields.add(new TextField("sinceDate",
//                "sinceDate",
//                format.format(new Date())));
//
//
//        auxFields.add(new TextField("expiryDate",
//                "expiryDate",
//                format.format(new Date())));
//
//
//        if (auxFields.size() > 0) {
//            info.setAuxiliaryFields(auxFields);
//        }
//
//        List<Field<?>> backFields = new ArrayList<>();
//        backFields.add(new TextField("supportEmail", "Support Email", "arpit.manglik@entrust.com"));
//        backFields.add(new TextField("companyAddress", "Company Address", "EDC"));
//
//        info.setBackFields(backFields);
//
//        pass.passInformation(info);
//        PassSigner signer = setupSignerAndTeamId(pass);
//
//        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
//            PassSerializer.writePkPassArchive(pass, signer, baos);
//            return ResponseEntity.ok().body(baos.toByteArray());
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }
//
//
//    }


    @GetMapping(path = "/createNew", produces = "application/vnd.apple.pkpass")
    public ResponseEntity<byte[]> createFromNewPassKit() {
        try {

            PKBarcodeBuilder barcodeBuilder=PKBarcode.builder()
                    .format(PKBarcodeFormat.PKBarcodeFormatPDF417)
                    .message("BarcodeValue")
                    .messageEncoding("UTF-8")
                    .altText("BarcodeValue");

            PKField secField=PKField.builder()
                    .key("userRole")
                    .label("Role")
                    .value("Admin")
                    .build();


            URL resourceUrl = getClass().getResource("/SampleJpg.jpg");
            PKPassTemplateInMemory pkPassTemplateInMemory = new PKPassTemplateInMemory();
            pkPassTemplateInMemory.addFile(PKPassTemplateInMemory.PK_ICON, resourceUrl);
            pkPassTemplateInMemory.addFile(PKPassTemplateInMemory.PK_ICON_RETINA, resourceUrl);
            pkPassTemplateInMemory.addFile(PKPassTemplateInMemory.PK_THUMBNAIL, resourceUrl);
            pkPassTemplateInMemory.addFile(PKPassTemplateInMemory.PK_THUMBNAIL_RETINA, resourceUrl);
            pkPassTemplateInMemory.addFile(PKPassTemplateInMemory.PK_LOGO, resourceUrl);
            pkPassTemplateInMemory.addFile(PKPassTemplateInMemory.PK_LOGO_RETINA, resourceUrl);
            List<PKField> auxFields = new ArrayList<>();
            //SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            auxFields.add(PKField.builder().key("userNumber").label("User Number").value("1").build());
            auxFields.add(PKField.builder().key("sinceDate").label("Since Date").value("2022-04-01").build());
            auxFields.add(PKField.builder().key("expiryDate").label("Expiry Date").value(Instant.parse("2022-04-08T19:40:00.00Z")).build());

            List<PKField> backFields = new ArrayList<>();
            backFields.add(PKField.builder().key("supportEmail").label("Support Email").value("arpit.manglik@entrust.com").build());
            backFields.add(PKField.builder().key("companyAddress").label("Company Address").value("EDC").build());



            PKPass pkPass = PKPass.builder()
                    .pass(
                            PKGenericPass.builder()
                                    .passType(PKPassType.PKGenericPass)
                                    .primaryFieldBuilder(
                                            PKField.builder()
                                                    .key("userFullName")
                                                    .label("Full Name")
                                                    .value("Arpit MAnglik")
                                    )
                                    .headerFieldBuilder(
                                            PKField.builder()
                                                    .key("passTitle")
                                                    .value("MFPT")
                                    )
                                    .secondaryFieldBuilder(
                                            PKField.builder()
                                                    .key("userRole")
                                                    .label("Role")
                                                    .value("Admin")
                                    )
                                    .auxiliaryFields(auxFields)
                                    .backFields(backFields)
                    )
                    .barcodeBuilder(barcodeBuilder)
                    .formatVersion(1)
                    .passTypeIdentifier("pass.com.entrustdatacard.idissuance")
                    .teamIdentifier("79D8S444P6")
                    .serialNumber("000000001")
                    .organizationName("EDC")
                    .logoText("EDC")
                    .description("MFPT")
                    .backgroundColor(java.awt.Color.black)
                    .foregroundColor("rgb(255,255,255)")
                    .labelColor(java.awt.Color.red)
                    .webServiceURL(new URL("https://apple-pass-poc.herokuapp.com/pass/apple/register"))
                    .authenticationToken("1111111111111111")
                    .sharingProhibited(true)
                    .expirationDate(Instant.parse("2022-04-12T08:30:00.00Z"))
                    .build();

            byte[] signedAndZippedPkPassArchive = createPkPassArchive(pkPass, pkPassTemplateInMemory);
            return new ResponseEntity(signedAndZippedPkPassArchive, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    private byte[] createPkPassArchive(PKPass pass, PKPassTemplateInMemory pkPassTemplateInMemory) {
        try {
            InputStream is = getClass().getResourceAsStream("/Instant_IDaaS_Mobile_Flash_Pass_Oct_2021.p12");
            String certString = Base64.encodeBase64String(IOUtils.toByteArray(is));
            KeyStore keystore = MobileFlashPassSettingsUtils.getAppleWalletKeystore(certString, "XPhuVzH5LJVqGLcwS6vH4pYGCUmHUAvv");
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            X509Certificate intermediateCert = (X509Certificate) certificateFactory.generateCertificate(getClass().getResourceAsStream("/AppleWWDR.cer"));

            PrivateKeyAndCert privateKeyAndCert = new PrivateKeyAndCert();
            privateKeyAndCert = MobileFlashPassSettingsUtils.getAppleWalletPrivateKeyAndCert(keystore, "XPhuVzH5LJVqGLcwS6vH4pYGCUmHUAvv");

            //Pair<PrivateKey, X509Certificate> pair = CertUtils.extractCertificateWithKey(keystore, "XPhuVzH5LJVqGLcwS6vH4pYGCUmHUAvv" .toCharArray());
            PKSigningInformation pkSigningInformation = new PKSigningInformation(privateKeyAndCert.getPublicCert(), privateKeyAndCert.getPrivateKey(), intermediateCert);
            PKFileBasedSigningUtil pkSigningUtil = new PKFileBasedSigningUtil();
            return pkSigningUtil.createSignedAndZippedPkPassArchive(pass, pkPassTemplateInMemory, pkSigningInformation);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
//
    @PostMapping(path = "/register/v1/devices/{deviceId}/registrations/{passType}/{serial}")
    public void replyOnClaim(HttpServletRequest request, @PathVariable("deviceId") String deviceId, @PathVariable("passType") String passType, @PathVariable("serial") String serial, @RequestBody PKPushToken push) {
        System.out.println("Reached register function");
        System.out.println("DeviceId is: " + deviceId);
        System.out.println("Pass Type is: " + passType);
        System.out.println("Serial Number is: " + serial);
        String authToken = request.getHeader("Authorization");
        System.out.println("Auth token is: " + authToken);
        System.out.println("PushToken is: " + push.getPushToken());

    }

    @PostMapping(path="/pushNotification/{pushToken}")
    public void  sendPush(@PathVariable("pushToken") String pushToken) {
        System.out.println("In send push function: " + pushToken);
        try {
            InputStream is = getClass().getResourceAsStream("/Instant_IDaaS_Mobile_Flash_Pass_Oct_2021.p12");
            String certString = Base64.encodeBase64String(IOUtils.toByteArray(is));
            KeyStore keystore = MobileFlashPassSettingsUtils.getAppleWalletKeystore(certString, "XPhuVzH5LJVqGLcwS6vH4pYGCUmHUAvv");

            AppleFlashPassPushNotificationUtil appleFlashPassPushNotificationUtil = new AppleFlashPassPushNotificationUtil(keystore);
            appleFlashPassPushNotificationUtil.sendPushNotificationAsync(pushToken);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @PostMapping(path="/register/v1/log")
    public void logPass(@RequestBody LogEntries logEntries) {
        System.out.println("Reached log function");
        if (logEntries.getLogs().length > 0) {
            System.out.println(Arrays.stream(logEntries.getLogs()).findFirst().get());
        }
    }

    @DeleteMapping(path = "/register/v1/devices/{deviceId}/registrations/{passType}/{serial}")
    public void deletePass(HttpServletRequest request, @PathVariable("deviceId") String deviceId, @PathVariable("passType") String passType, @PathVariable("serial") String serial) {
        System.out.println("Reached delete function");
        System.out.println("DeviceId is: " + deviceId);
        System.out.println("Pass Type is: " + passType);
        System.out.println("Serial Number is: " + serial);
        String authToken = request.getHeader("Authorization");
        System.out.println("Auth token is: " + authToken);
    }

    @GetMapping(path = "/register/v1/devices/{deviceId}/registrations/{passType}", produces = "application/json")
    public ResponseEntity<SerialNumbers> getPass(HttpServletRequest request, @PathVariable("deviceId") String deviceId, @PathVariable("passType") String passType, @RequestParam(required = false) String passesUpdatedSince) {
        System.out.println("Reached get function after push");
        System.out.println("DeviceId is: " + deviceId);
        System.out.println("Pass Type is: " + passType);
        String authToken = request.getHeader("Authorization");
        System.out.println("Auth token is: " + authToken);
        System.out.println("Previous updated is: " + passesUpdatedSince);
        System.out.println(request.getQueryString());

        SerialNumbers serialNumbers = new SerialNumbers();
        String[] serials = new String[1];
        serials[0] = "000000001";
        serialNumbers.setSerialNumbers(serials);

        serialNumbers.setLastUpdated(OffsetDateTime.now().toString());
        System.out.println(serialNumbers.toString());

        //return serialNumbers;
        return new ResponseEntity(serialNumbers, HttpStatus.OK);
        //return ResponseEntity.ok().body(serialNumbers);
    }
//
//    private PassSignerImpl setupSignerAndTeamId(Pass pass) {
//        PrivateKeyAndCert privateKeyAndCert = null;
//        try {
//            InputStream is = getClass().getResourceAsStream("/Instant_IDaaS_Mobile_Flash_Pass_Oct_2021.p12");
//            String certString = Base64.encodeBase64String(IOUtils.toByteArray(is));
//            KeyStore keystore = MobileFlashPassSettingsUtils.getAppleWalletKeystore(certString, "XPhuVzH5LJVqGLcwS6vH4pYGCUmHUAvv");
//            privateKeyAndCert = MobileFlashPassSettingsUtils.getAppleWalletPrivateKeyAndCert(keystore, "XPhuVzH5LJVqGLcwS6vH4pYGCUmHUAvv");
//            pass.teamIdentifier(MobileFlashPassSettingsUtils.getAppleWalletTeamIdFromCert(privateKeyAndCert.getPublicCert()));
//            MobileFlashPassSettingsUtils.validateAppleWalletCertExpiryDate(privateKeyAndCert.getPublicCert());
//
//
//            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
//            X509Certificate intermediateCert = (X509Certificate) certificateFactory.generateCertificate(getClass().getResourceAsStream("/AppleWWDR.cer"));
//            return new PassSignerImpl(privateKeyAndCert.getPublicCert(), privateKeyAndCert.getPrivateKey(), intermediateCert);
//        } catch (Exception e) {
//            return null;
//        }
//    }
//
//    protected byte[] getImageBytes(String base64Image) {
//        if (base64Image == null) return new byte[0];
//
//        if (base64Image.startsWith("data:image")) {
//            int startIndex = base64Image.indexOf(',') + 1;
//            if (base64Image.length() > startIndex) {
//                return Base64.decodeBase64(base64Image.substring(startIndex));
//            } else {
//                // Invalid
//                return new byte[0];
//            }
//        } else {
//            return Base64.decodeBase64(base64Image);
//        }
//    }

//    protected byte[] downsample(byte[] pngImage, float percentOfOriginal) {
//        try {
//            if (pngImage != null) {
//                String base64String = new String(pngImage);
//                if (base64String.contains("base64")) {
//                    String base64Image = base64String.split(",")[1];
//                    pngImage = javax.xml.bind.DatatypeConverter.parseBase64Binary(base64Image);
//                }
//                BufferedImage sourceImage = ImageIO.read(new ByteArrayInputStream(pngImage));
//
//                Image scaledImage = sourceImage.getScaledInstance(
//                        Math.round(sourceImage.getWidth() * percentOfOriginal),
//                        Math.round(sourceImage.getHeight() * percentOfOriginal),
//                        Image.SCALE_SMOOTH);
//                RenderedImage scaledBufferedImage;
//                if (RenderedImage.class.isAssignableFrom(scaledImage.getClass()))
//                    scaledBufferedImage = (RenderedImage) scaledImage;
//                else {
//                    scaledBufferedImage = new BufferedImage(scaledImage.getWidth(null), scaledImage.getHeight(null), BufferedImage.TYPE_INT_ARGB);
//                    final Graphics2D g2 = ((BufferedImage) scaledBufferedImage).createGraphics();
//                    g2.drawImage(scaledImage, null, null);
//                    g2.dispose();
//                }
//
//                try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
//                    ImageIO.write(scaledBufferedImage, "PNG", baos);
//                    return baos.toByteArray();
//                }
//            }
//        } catch (IOException e) {
//            throw new IllegalArgumentException("Unable to downsample given image.", e);
//        }
//        return pngImage;
//    }

    @GetMapping(path = "/register/v1/passes/{passType}/{serialNumber}", produces = "application/vnd.apple.pkpass")
    public ResponseEntity<byte[]> updateFromNewPassKit() {

        try {
            List<PKField> auxFields = new ArrayList<>();
            //SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            auxFields.add(PKField.builder().key("userNumber").label("User Number").value("1").build());
            auxFields.add(PKField.builder().key("sinceDate").label("Since Date").value("2022-04-01").build());
            auxFields.add(PKField.builder().key("expiryDate").label("Expiry Date").value(Instant.parse("2022-04-08T19:40:00.00Z")).build());

            List<PKField> backFields = new ArrayList<>();
            backFields.add(PKField.builder().key("supportEmail").label("Support Email").value("arpit.manglik@entrust.com").build());
            backFields.add(PKField.builder().key("companyAddress").label("Company Address").value("EDC").build());

            //InputStream imageStream = getClass().getResourceAsStream("/SampleJpg.jpg");
            //byte[] logo2x = IOUtils.toByteArray(getClass().getResourceAsStream("/SampleJpg.jpg"));

            URL resourceUrl = getClass().getResource("/SampleJpg.jpg");
            System.out.println(resourceUrl.toString());
            PKPassTemplateInMemory pkPassTemplateInMemory = new PKPassTemplateInMemory();
            pkPassTemplateInMemory.addFile(PKPassTemplateInMemory.PK_ICON, resourceUrl);
            pkPassTemplateInMemory.addFile(PKPassTemplateInMemory.PK_ICON_RETINA, resourceUrl);
            pkPassTemplateInMemory.addFile(PKPassTemplateInMemory.PK_THUMBNAIL, resourceUrl);
            pkPassTemplateInMemory.addFile(PKPassTemplateInMemory.PK_THUMBNAIL_RETINA, resourceUrl);
            pkPassTemplateInMemory.addFile(PKPassTemplateInMemory.PK_LOGO, resourceUrl);
            pkPassTemplateInMemory.addFile(PKPassTemplateInMemory.PK_LOGO_RETINA, resourceUrl);


            PKPass pkPass = PKPass.builder()
                    .pass(
                            PKGenericPass.builder()
                                    .passType(PKPassType.PKGenericPass)
                                    .primaryFieldBuilder(
                                            PKField.builder()
                                                    .key("userFullName")
                                                    .label("Full Name")
                                                    .value("Arpit Manglik Updated")
                                    )
                                    .headerFieldBuilder(
                                            PKField.builder()
                                                    .key("passTitle")
                                                    .value("MFPT")
                                    )
                                    .secondaryFieldBuilder(
                                            PKField.builder()
                                                    .key("userRole")
                                                    .label("Role")
                                                    .value("Admin")
                                    )
                                    .auxiliaryFields(auxFields)
                                    .backFields(backFields)
                    )
                    .barcodeBuilder(
                            PKBarcode.builder()
                                    .format(PKBarcodeFormat.PKBarcodeFormatPDF417)
                                    .message("BarcodeValue")
                                    .messageEncoding("UTF-8")
                                    .altText("BarcodeValue")
                    )
                    .formatVersion(1)
                    .passTypeIdentifier("pass.com.entrustdatacard.idissuance")
                    .teamIdentifier("79D8S444P6")
                    .serialNumber("000000001")
                    .organizationName("EDC")
                    .logoText("EDC")
                    .description("MFPT")
                    .backgroundColor(java.awt.Color.black)
                    .foregroundColor("rgb(255,255,255)")
                    .labelColor(java.awt.Color.red)
                    .webServiceURL(new URL("https://apple-pass-poc.herokuapp.com/pass/apple/register"))
                    .authenticationToken("1111111111111111")
                    .sharingProhibited(true)
                    .expirationDate(Instant.parse("2022-04-12T08:30:00.00Z"))
                    .build();

            byte[] signedAndZippedPkPassArchive = createPkPassArchive(pkPass, pkPassTemplateInMemory);
            HttpHeaders headers = new HttpHeaders();
            headers.setLastModified(OffsetDateTime.now().toInstant());
            return new ResponseEntity(signedAndZippedPkPassArchive, headers, HttpStatus.OK);
            //return ResponseEntity.ok().body(signedAndZippedPkPassArchive);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }
}


