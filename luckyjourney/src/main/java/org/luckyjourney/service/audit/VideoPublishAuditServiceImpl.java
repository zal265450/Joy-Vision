package org.luckyjourney.service.audit;

import org.luckyjourney.config.QiNiuConfig;
import org.luckyjourney.constant.AuditStatus;
import org.luckyjourney.entity.response.AuditResponse;
import org.luckyjourney.entity.response.VideoAuditResponse;
import org.luckyjourney.entity.task.VideoTask;
import org.luckyjourney.entity.video.Video;
import org.luckyjourney.mapper.video.VideoMapper;
import org.luckyjourney.service.FileService;
import org.luckyjourney.service.InterestPushService;
import org.luckyjourney.service.audit.AbstractAuditService;
import org.luckyjourney.util.FileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotBlank;
import java.util.function.Supplier;

/**
 * @description: 视频发布审核
 * @Author: Xhy
 * @CreateTime: 2023-10-29 14:40
 */
@Service
public class VideoPublishAuditServiceImpl extends AbstractAuditService<VideoTask> {


    @Autowired
    private VideoMapper videoMapper;

    @Autowired
    private InterestPushService interestPushService;

    @Autowired
    private FileService fileService;

    /**
     *
     * @param videoTask
     * @param auditQueueState 申请快/慢审核
     * @return
     */
    public VideoTask audit(VideoTask videoTask,Boolean auditQueueState){
        if (auditQueueState){
            new Thread(()->{
                audit(videoTask);
            }).start();
        }else {
            audit(videoTask);
        }
        return null;
    }

    // 进行任务编排
    @Override
    public VideoTask audit(VideoTask videoTask) {
        executor.submit(()->{
            final Video video = videoTask.getVideo();
            final String url = QiNiuConfig.CNAME+"/"+video.getUrl();
            // 只有视频在新增或者公开时候才需要调用审核视频/封面
            // 新增 ： 必须审核
            // 修改: 新老状态不一致
            // 需要审核视频/封面
            boolean needAuditVideo = false;
            if (videoTask.getIsAdd()  && videoTask.getOldState() == videoTask.getNewState()){
                needAuditVideo = true;
            }else if (!videoTask.getIsAdd() && videoTask.getOldState() != videoTask.getNewState()){
                // 修改的情况下新老状态不一致,说明需要更新
                if (!videoTask.getNewState()){
                   needAuditVideo = true;
                }
            }
            AuditResponse videoAuditResponse = new AuditResponse(AuditStatus.SUCCESS,"正常");
            AuditResponse coverAuditResponse = new AuditResponse(AuditStatus.SUCCESS,"正常");
            AuditResponse titleAuditResponse = new AuditResponse(AuditStatus.SUCCESS,"正常");
            AuditResponse descAuditResponse = new AuditResponse(AuditStatus.SUCCESS,"正常");
            if (needAuditVideo){
                  videoAuditResponse = auditVideo(url);
                  coverAuditResponse = auditImage(video.getCover());
                interestPushService.pushSystemTypeStockIn(video);
                interestPushService.pushSystemStockIn(video);
                final String duration = FileUtil.getVideoDuration(url);
                video.setDuration(duration);
                // 填充视频类型
                video.setVideoType(fileService.getFileInfo(url).mimeType);
            }else if (videoTask.getNewState()){
                interestPushService.deleteSystemStockIn(video);
                interestPushService.deleteSystemTypeStockIn(video);
            }
            // 新老视频标题简介一致
            final Video oldVideo = videoTask.getOldVideo();
            if (!video.getTitle().equals(oldVideo.getTitle())) {
                titleAuditResponse = auditText(video.getTitle());
            }
            if (!video.getDescription().equals(oldVideo.getDescription())){
                descAuditResponse = auditText(video.getDescription());
            }

            final Integer videoAuditStatus = videoAuditResponse.getAuditStatus();
            final Integer coverAuditStatus = coverAuditResponse.getAuditStatus();
            final Integer titleAuditStatus = titleAuditResponse.getAuditStatus();
            final Integer descAuditStatus = descAuditResponse.getAuditStatus();
            boolean f1 = videoAuditStatus == AuditStatus.SUCCESS;
            boolean f2 = coverAuditStatus == AuditStatus.SUCCESS;
            boolean f3 = titleAuditStatus == AuditStatus.SUCCESS;
            boolean f4 = descAuditStatus == AuditStatus.SUCCESS;

            if (f1 && f2 && f3 && f4) {
                video.setMsg("通过");
                video.setAuditStatus(AuditStatus.SUCCESS);
                // 填充视频时长
            }else {
                video.setAuditStatus(AuditStatus.PASS);
                // 避免干扰
                video.setMsg("");
                if (!f1){
                    video.setMsg("视频有违规行为: "+videoAuditResponse.getMsg());
                }
                if (!f2){
                    video.setMsg(video.getMsg()+"\n封面有违规行为: " + coverAuditResponse.getMsg());
                }
                if (!f3){
                    video.setMsg(video.getMsg()+"\n标题有违规行为: " + titleAuditResponse.getMsg());
                }
                if (!f4){
                    video.setMsg(video.getMsg()+"\n简介有违规行为: " + descAuditResponse.getMsg());
                }
            }

            videoMapper.updateById(video);
        });

        return null;
    }
}
