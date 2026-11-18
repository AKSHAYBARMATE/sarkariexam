package com.helixion.sarkariPariksha.Controller;

import com.helixion.sarkariPariksha.Entity.CandidateProfile;
import com.helixion.sarkariPariksha.Request.CandidatesRequest;
import com.helixion.sarkariPariksha.Response.CandidateProfileResponse;
import com.helixion.sarkariPariksha.Security.LoginUser;
import com.helixion.sarkariPariksha.Services.CandidateServices;
import com.helixion.sarkariPariksha.Utils.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("Candidate")
@Tag(name = "Candidate Controller", description = "APIs for Candidate profile and information management")
public class CandidateController {

    @Autowired
    private CandidateServices candidateServices;
    @Autowired
    private LoginUser loginUser;

    @PostMapping("/saveProfile")
    @Operation(summary = "Save or Update Candidate Profile", description = "Saves candidate personal, educational, and location details, and associates them with a user account.")
    public BaseResponse<CandidateProfile> saveCandidatesProfile(@RequestBody CandidatesRequest candidatesRequest) {
        return candidateServices.saveCandidatesProfile(candidatesRequest);
    }

    @GetMapping("/getProfileById/{id}")
    @Operation(summary = "Get Candidate Profile By ID", description = "Fetches candidate profile details by candidate ID")
    public BaseResponse<CandidateProfileResponse> getCandidateProfileById(@PathVariable Long id) {
        return candidateServices.getCandidateProfileById(id);
    }
    @GetMapping("/getProfileById")
    @Operation(summary = "Get Logged In Candidate Profile", description = "Fetches candidate profile details for the logged-in candidate from JWT token")
    public BaseResponse<CandidateProfileResponse> getCandidateProfile() {
        if (loginUser == null || loginUser.getCandidateId() == null) {
            BaseResponse<CandidateProfileResponse> response = new BaseResponse<>();
            response.setStatus(0);
            response.setCode(401);
            response.setMessage("Candidate profile not found or user not authenticated");
            return response;
        }
        return candidateServices.getCandidateProfileById(loginUser.getCandidateId());
    }
}
