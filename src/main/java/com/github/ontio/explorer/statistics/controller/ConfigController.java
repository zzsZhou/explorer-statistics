package com.github.ontio.explorer.statistics.controller;

import com.github.ontio.explorer.statistics.common.Response;
import com.github.ontio.explorer.statistics.common.Result;
import com.github.ontio.explorer.statistics.service.ConfigService;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequestMapping("v2/config")
public class ConfigController {

    private ConfigService configService;

    public ConfigController(ConfigService configService) {
        this.configService = configService;
    }

    @ApiOperation(value = "Get max staking change count from DB")
    @GetMapping(value = "/max-staking-change-count")
    public Response getmaxStakingChangeCount() {
        String count = configService.getmaxStakingChangeCount();
        if (count == null || count.length() == 0) {
            return new Response(Result.INTERNAL_SERVER_ERROR);
        }
        return new Response(Result.SUCCESS, count);
    }

    @ApiOperation(value = "Update max staking change count from Blockchain")
    @PutMapping(value = "/max-staking-change-count")
    public Response refreshBlockCountInStakingRound() {
        String count = configService.updateMaxStakingChangeCount();
        if (count == null || count.length() == 0) {
            return new Response(Result.INTERNAL_SERVER_ERROR);
        }
        return new Response(Result.SUCCESS, count);
    }




}
